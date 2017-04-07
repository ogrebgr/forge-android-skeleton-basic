package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.AppLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class ResMainImpl extends AbstractMultiOperationResidentComponent<ResMain.Operation> implements ResMain,
        ForgeExchangeManagerListener, AppLoginHelper.Listener, FacebookLoginHelper.Listener {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final AppConfiguration mAppConfiguration;
    private final NetworkInfoProvider mNetworkInfoProvider;
    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;
    private final CurrentUserHolder mCurrentUserHolder;
    private final Provider<AppLoginHelper> mLoginHelperProvider;
    private final FacebookLoginHelper mFacebookLoginHelper;
    private volatile long mAutoRegisterXId;
    private int mLoginError;
    private AutoregisteringError mAutoregisteringError;

    private AppLoginHelper mAppLoginHelper;


    @Inject
    public ResMainImpl(
            AppConfiguration appConfiguration,
            ForgeExchangeHelper forgeExchangeHelper,
            Session session,
            NetworkInfoProvider networkInfoProvider,
            CurrentUserHolder currentUserHolder,
            Provider<AppLoginHelper> loginHelperProvider,
            FacebookLoginHelper facebookLoginHelper) {


        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
        mCurrentUserHolder = currentUserHolder;
        mLoginHelperProvider = loginHelperProvider;
        mFacebookLoginHelper = facebookLoginHelper;
    }


    public AutoregisteringError getAutoregisteringError() {
        return mAutoregisteringError;
    }


    @Override
    public CurrentUser getCurrentUser() {
        return mCurrentUserHolder.getCurrentUser();
    }


    @Override
    public void onLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onLoginFail(int code) {
        mLoginError = code;
        switchToEndedStateFail();
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
    public void autoLoginIfNeeded() {
        if (mNetworkInfoProvider.isConnected()) {
            if (mAppConfiguration.getLoginPrefs().hasLoginCredentials()) {
                if (mAppConfiguration.getAppPrefs().getSelectedLoginMethod() != null) {
                    loginActual();
                }
            } else {
                if (mAppConfiguration.shallAutoregister()) {
                    autoRegister();
                }
            }
        }
    }


    @Override
    public void login() {
        loginActual();
    }


    @Override
    public void abortLogin() {
        if (mAppLoginHelper != null) {
            mAppLoginHelper.abortLogin();
        }
        abort();
    }


    @Override
    public void logout() {
        switchToBusyState(Operation.LOGOUT);
        mSession.logout();
        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("logout");
        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        em.executeExchange(b.build(), em.generateTaskId());

        switchToEndedStateSuccess();
    }

//
//    private void loginFacebook() {
//        AccessToken token = AccessToken.getCurrentAccessToken();
//        if (token == null) {
//            loginFacebook();
//        } else {
//            checkFbLogin(token.getToken(), token.getUserId());
//        }
//    }


    @Override
    public int getLoginError() {
        return mLoginError;
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mAutoRegisterXId) {
            handleAutoRegisterOutcome(isSuccess, result);
        } else {
            if (mAppLoginHelper != null) {
                mAppLoginHelper.handleExchange(exchangeId, isSuccess, result);
            }
        }
    }


    private void autoRegister() {
        if (isIdle()) {
            switchToBusyState(Operation.AUTO_REGISTERING);
            ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("autoregister");
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
            b.addPostParameter("session_info", "1");

            ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
            mAutoRegisterXId = em.generateTaskId();
            em.executeExchange(b.build(), mAutoRegisterXId);
        } else {
            throw new IllegalStateException("Not in IDLE");
        }
    }


    private void loginActual() {
        if (isIdle()) {
            switchToBusyState(Operation.LOGIN);

            switch (mAppConfiguration.getAppPrefs().getLastSuccessfulLoginMethod()) {
                case APP:
                    loginApp();
                    break;
                case GOOGLE:
                    break;
                case FACEBOOK:
                    //                loginFacebook();
                    break;
            }
        } else {
            throw new IllegalStateException("Not in IDLE");
        }
    }


    private void loginApp() {
        mAppLoginHelper = mLoginHelperProvider.get();
        mAppLoginHelper.initiate(mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                mAppConfiguration.getLoginPrefs().getUsername(),
                mAppConfiguration.getLoginPrefs().getPassword(),
                this,
                true);
    }


    private void handleAutoRegisterOutcome(boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();

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

                        LoginPrefs lp = mAppConfiguration.getLoginPrefs();

                        lp.setUsername(jobj.getString("username"));
                        lp.setPassword(jobj.getString("password"));
                        lp.setManualRegistration(false);
                        lp.save();

                        mAppConfiguration.getAppPrefs().setSelectedLoginMethod(LoginMethod.APP);
                        mAppConfiguration.getAppPrefs().save();

                        if (mAppConfiguration.shallUseGcm()) {
                            processGcmToken();
                        } else {
                            switchToEndedStateSuccess();
                        }
                    } else {
                        mLogger.error("Missing session info");
                        mAutoregisteringError = AutoregisteringError.FAILED;
                        switchToEndedStateFail();
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    mAutoregisteringError = AutoregisteringError.FAILED;
                    switchToEndedStateFail();

                }
            } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED) {
                mLogger.warn("Upgrade needed");
                mAutoregisteringError = AutoregisteringError.UPGRADE_NEEDED;
                switchToEndedStateFail();
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                mAutoregisteringError = AutoregisteringError.FAILED;
                switchToEndedStateFail();
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            mAutoregisteringError = AutoregisteringError.FAILED;
            switchToEndedStateFail();
        }
    }


    private void processGcmToken() {

    }
}
