package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class ResSelectLoginImpl extends AbstractMultiOperationResidentComponent<ResSelectLogin.Operation>
        implements ResSelectLogin, FacebookLoginHelper.Listener {


    private volatile long mFacebookCheckXId;
    private volatile long mGoogleCheckXId;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;
    private final ForgeExchangeHelper mForgeExchangeHelper;

    private final FacebookLoginHelper mFacebookLoginHelper;
    private final CurrentUserHolder mCurrentUserHolder;
    private final Session mSession;

    @Inject
    public ResSelectLoginImpl(FacebookLoginHelper facebookLoginHelper,
                              ForgeExchangeHelper forgeExchangeHelper,
                              AppConfiguration appConfiguration,
                              CurrentUserHolder currentUserHolder,
                              Session session) {

        mFacebookLoginHelper = facebookLoginHelper;
        mForgeExchangeHelper = forgeExchangeHelper;
        mAppConfiguration = appConfiguration;
        mCurrentUserHolder = currentUserHolder;
        mSession = session;
    }


    @Override
    public void checkFbLogin(String token) {
        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState(Operation.FACEBOOK_LOGIN);
            mFacebookLoginHelper.checkFbLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_facebook"), this, token);
        }
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mGoogleCheckXId) {
            handleGoogleCheckResult(isSuccess, result);
        } else {
            mFacebookLoginHelper.handleExchange(exchangeId, isSuccess, result);
        }
    }


    @Override
    public void onFacebookLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onFacebookLoginFail(int code) {
        switchToEndedStateFail();
    }


    @Override
    public void checkGoogleLogin(String token) {
        mLogger.debug("Got google token", token);


        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState(Operation.GOOGLE_LOGIN);
            ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login_google.php");

            b.addPostParameter("token", token);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
            b.addPostParameter("session_info", "1");

            if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
                b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
            }

            ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
            mGoogleCheckXId = em.generateTaskId();
            em.executeExchange(b.build(), mGoogleCheckXId);
        } else {
            mLogger.warn("checkGoogleLogin(): Not in state IDLE");
        }
    }


    private void handleGoogleCheckResult(boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();
            if (code > 0) {
                if (code == BasicResponseCodes.OK) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            mSession.startSession(sessionTtl);

                            mCurrentUserHolder.setCurrentUser(
                                    new CurrentUser(sessionInfo.getLong("user_id"),
                                            sessionInfo.optString("screen_name", null)));


                            mLogger.debug("Google login OK");

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.GOOGLE);
                            mAppConfiguration.getAppPrefs().save();

                            switchToEndedStateSuccess();
                        } else {
                            mLogger.error("Missing session info");
                            switchToEndedStateFail();
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Google login FAIL. JSON error:", result.getPayload());
                        switchToEndedStateFail();
                    }
                } else {
                    mLogger.debug("Google login FAIL. Code: {}", code);
                    switchToEndedStateFail();
                }
            } else {
                mLogger.debug("Google login FAIL. Code: {}", code);
                switchToEndedStateFail();
            }
        } else {
            mLogger.debug("Google login FAIL");
            switchToEndedStateFail();
        }
    }
}
