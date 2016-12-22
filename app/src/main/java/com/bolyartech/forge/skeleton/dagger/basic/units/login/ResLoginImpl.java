package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.AbstractSideEffectOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginHelper;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2016-01-05 14:26
 */
public class ResLoginImpl extends AbstractSideEffectOperationResidentComponent<Void, Integer> implements ResLogin,
        LoginHelper.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final ForgeExchangeHelper mForgeExchangeHelper;

    private LoginHelper mLoginHelper;

    Provider<LoginHelper> mLoginHelperProvider;


    @Inject
    public ResLoginImpl(ForgeExchangeHelper forgeExchangeHelper, Provider<LoginHelper> loginHelperProvider) {
        mForgeExchangeHelper = forgeExchangeHelper;
        mLoginHelperProvider = loginHelperProvider;
    }


    @Override
    public void login(String username, String password) {
        if (isIdle()) {
            switchToBusyState();

            mLoginHelper = mLoginHelperProvider.get();
            mLoginHelper.initiate(mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                    mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                    username,
                    password,
                    this,
                    false);
        }
    }


    @Override
    public void abortLogin() {
        if (mLoginHelper != null) {
            mLoginHelper.abortLogin();
        }
        abort();
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mLoginHelper != null) {
            mLoginHelper.handleExchange(exchangeId, isSuccess, result);
        }
    }


    @Override
    public void onLoginOk() {
        switchToEndedStateSuccess(null);
    }


    @Override
    public void onInvalidLogin() {
        switchToEndedStateFail(AuthenticationResponseCodes.Errors.INVALID_LOGIN);
    }


    @Override
    public void onUpgradeNeeded() {
        switchToEndedStateFail(BasicResponseCodes.Errors.UPGRADE_NEEDED);
    }


    @Override
    public void onLoginFail() {
        switchToEndedStateFail(null);
    }


//    private void handleStep1(boolean isSuccess, ForgeExchangeResult result) {
//        if (!mAbortLogin) {
//            if (isSuccess) {
//                int code = result.getCode();
//
//                if (code > 0) {
//                    if (code == BasicResponseCodes.OK) {
//                        try {
//                            JSONObject jobj = new JSONObject(result.getPayload());
//                            int sessionTtl = jobj.getInt("session_ttl");
//                            JSONObject sessionInfo = jobj.optJSONObject("session_info");
//                            if (sessionInfo != null) {
//                                mSession.startSession(sessionTtl);
//
//                                mCurrentUserHolder.setCurrentUser(
//                                        new CurrentUser(sessionInfo.getLong("user_id"),
//                                                sessionInfo.optString("screen_name_chosen", null)));
//
//                                mLogger.debug("App login OK");
//
//                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
//                                mAppConfiguration.getAppPrefs().save();
//
//                                LoginPrefs lp = mAppConfiguration.getLoginPrefs();
//                                lp.setUsername(mLastUsedUsername);
//                                lp.setPassword(mLastUsedPassword);
//                                lp.setManualRegistration(true);
//                                lp.save();
//
//                                switchToEndedStateSuccess(null);
//                            } else {
//                                mLogger.error("Missing session info");
//                                switchToEndedStateFail(null);
//
//                            }
//                        } catch (JSONException e) {
//                            mLogger.warn("Login exchange failed because cannot parse JSON");
//                            switchToEndedStateFail(null);
//                        }
//                    } else {
//                        // unexpected positive code
//                        switchToEndedStateFail(null);
//                    }
//                } else {
//
//                    mLogger.warn("Login exchange failed with code {}", code);
//                    switchToEndedStateFail(code);
//                }
//            } else {
//                switchToEndedStateFail(null);
//            }
//        }
//    }
}
