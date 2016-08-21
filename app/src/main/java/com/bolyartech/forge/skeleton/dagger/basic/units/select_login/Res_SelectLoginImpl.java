package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Res_SelectLoginImpl extends AbstractMultiOperationResidentComponent<Res_SelectLogin.Operation> implements Res_SelectLogin {
    private volatile long mFacebookCheckXId;
    private volatile long mGoogleCheckXId;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;

    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;

    private LoginResult mLoginResult;


    @Inject
    public Res_SelectLoginImpl(AppConfiguration appConfiguration,
                               ForgeExchangeHelper forgeExchangeHelper,
                               Session session
                               ) {


        mAppConfiguration = appConfiguration;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
    }




    @Override
    public void checkFbLogin(String token, String facebookUserId) {
        if (getOperationState() == OperationState.IDLE) {
            switchToBusyState(Operation.FACEBOOK_LOGIN);

            ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login_fb.php");

            b.addPostParameter("token", token);
            b.addPostParameter("user_id", facebookUserId);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
            b.addPostParameter("session_info", "1");

            if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
                b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
            }

            ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
            mFacebookCheckXId = em.generateTaskId();
            em.executeExchange(b.build(), mFacebookCheckXId);
        } else {
            mLogger.warn("checkFbLogin(): Not in state IDLE");
        }
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mFacebookCheckXId) {
            handleFbCheckResult(isSuccess, result);
        } else if (exchangeId == mGoogleCheckXId) {
            handleGoogleCheckResult(isSuccess, result);
        }
    }


    private void handleFbCheckResult(boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();
            if (code > 0) {
                if (code == BasicResponseCodes.Oks.OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            mSession.startSession(sessionTtl, new Session.Info(sessionInfo.getLong("user_id"),
                                    sessionInfo.getString("screen_name")));

                            mLogger.debug("Facebook login OK");

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.FACEBOOK);
                            mAppConfiguration.getAppPrefs().save();

                            mLoginResult = LoginResult.SUCCESS;
                            switchToCompletedState();
                        } else {
                            mLoginResult = LoginResult.FAIL;
                            switchToCompletedState();
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Facebook login FAIL. JSON error:", result.getPayload());
                        mLoginResult = LoginResult.FAIL;
                        switchToCompletedState();
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    mLoginResult = LoginResult.FAIL;
                    switchToCompletedState();
                }
            } else {
                mLogger.debug("Facebook login FAIL. Code: {}", code);
                mLoginResult = LoginResult.FAIL;
                switchToCompletedState();
            }
        } else {
            mLogger.debug("Facebook login FAIL");
            mLoginResult = LoginResult.FAIL;
            switchToCompletedState();
        }
    }


    @Override
    public void checkGoogleLogin(String token) {
        mLogger.debug("Got google token", token);


        if (getOperationState() == OperationState.IDLE) {
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
                if (code == BasicResponseCodes.Oks.OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            mSession.startSession(sessionTtl, new Session.Info(sessionInfo.getLong("user_id"),
                                    sessionInfo.getString("screen_name")));

                            mLogger.debug("Google login OK");

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.GOOGLE);
                            mAppConfiguration.getAppPrefs().save();

                            mLoginResult = LoginResult.SUCCESS;
                            switchToCompletedState();
                        } else {
                            mLogger.error("Missing session info");
                            mLoginResult = LoginResult.FAIL;
                            switchToCompletedState();
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Google login FAIL. JSON error:", result.getPayload());
                        mLoginResult = LoginResult.FAIL;
                        switchToCompletedState();
                    }
                } else {
                    mLogger.debug("Google login FAIL. Code: {}", code);
                    mLoginResult = LoginResult.FAIL;
                    switchToCompletedState();
                }
            } else {
                mLogger.debug("Google login FAIL. Code: {}", code);
                mLoginResult = LoginResult.FAIL;
                switchToCompletedState();
            }
        } else {
            mLogger.debug("Google login FAIL");
            mLoginResult = LoginResult.FAIL;
            switchToCompletedState();
        }
    }


    @Override
    public LoginResult getLoginResult() {
        return mLoginResult;
    }
}
