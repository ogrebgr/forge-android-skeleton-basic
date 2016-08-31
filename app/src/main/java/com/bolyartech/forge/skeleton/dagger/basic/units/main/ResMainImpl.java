package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgeGetHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthorizationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class ResMainImpl extends AbstractMultiOperationResidentComponent<ResMain.Operation> implements ResMain,
        ForgeExchangeManagerListener {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;
    private final NetworkInfoProvider mNetworkInfoProvider;

    private boolean mJustAutoregistered = false;

    private volatile long mAutoRegisterXId;
    private volatile long mLoginXId;
    private volatile boolean mAbortLogin = false;

    private LoginError mLoginError;
    private AutoregisteringError mAutoregisteringError;
    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;
    private final CurrentUserHolder mCurrentUserHolder;


    @Inject
    public ResMainImpl(
                        AppConfiguration appConfiguration,
                        ForgeExchangeHelper forgeExchangeHelper,
                        Session session,
                        NetworkInfoProvider networkInfoProvider,
                        CurrentUserHolder currentUserHolder) {


        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
        mCurrentUserHolder = currentUserHolder;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        if (mNetworkInfoProvider.isConnected()) {
            init();
        }
    }


    private void init() {
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
    public void onConnectivityChange() {
//        if (getParentState() == State.NO_INET) {
//            if (mNetworkInfoProvider.isConnected()) {
//                if (!getSession().isLoggedIn()) {
//                    init();
//                }
//            }
//        }
    }


    @Override
    public LoginError getLoginError() {
        return mLoginError;
    }


    public AutoregisteringError getAutoregisteringError() {
        return mAutoregisteringError;
    }


    private void autoRegister() {
        switchToBusyState(Operation.AUTO_REGISTERING);
        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("autoregister");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        mAutoRegisterXId = em.generateTaskId();
        em.executeExchange(b.build(), mAutoRegisterXId);
    }


    @Override
    public void login() {
        loginActual();
    }


    private void loginActual() {
        mAbortLogin = false;
        switchToBusyState(Operation.LOGIN);

        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login");
        b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
        b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        mLoginXId = em.generateTaskId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        switchToIdleState();
    }


    @Override
    public void logout() {
        mSession.logout();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ForgeGetHttpExchangeBuilder b = mForgeExchangeHelper.createForgeGetHttpExchangeBuilder("logout");
                ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
                em.executeExchange(b.build(), em.generateTaskId());
            }
        });
        t.start();
        switchToIdleState();
    }


    private void handleAutoRegisterOutcome(boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();

            if (code == BasicResponseCodes.Oks.OK.getCode()) {
                try {
                    JSONObject jobj = new JSONObject(result.getPayload());

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        int sessionTtl = jobj.getInt("session_ttl");
                        mSession.startSession(sessionTtl);

                        mCurrentUserHolder.setCurrentUser(new CurrentUser(sessionInfo.getLong("user_id"),
                                sessionInfo.getString("screen_name")));

                        LoginPrefs lp = mAppConfiguration.getLoginPrefs();

                        lp.setUsername(jobj.getString("username"));
                        lp.setPassword(jobj.getString("password"));
                        lp.setManualRegistration(false);
                        lp.save();

                        mAppConfiguration.getAppPrefs().setSelectedLoginMethod(LoginMethod.APP);
                        mAppConfiguration.getAppPrefs().save();

                        mJustAutoregistered = true;

                        if (mAppConfiguration.shallUseGcm()) {
                            processGcmToken();
                        } else {
                            switchToCompletedStateSuccess();
                        }
                    } else {
                        mLogger.error("Missing session info");
                        mAutoregisteringError = AutoregisteringError.FAILED;
                        switchToCompletedStateFail();
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    mAutoregisteringError = AutoregisteringError.FAILED;
                    switchToCompletedStateFail();

                }
            } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                mLogger.warn("Upgrade needed");
                mAutoregisteringError = AutoregisteringError.UPGRADE_NEEDED;
                switchToCompletedStateFail();
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                mAutoregisteringError = AutoregisteringError.FAILED;
                switchToCompletedStateFail();
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            mAutoregisteringError = AutoregisteringError.FAILED;
            switchToCompletedStateFail();
        }
    }


    private void processGcmToken() {

    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mAutoRegisterXId) {
            handleAutoRegisterOutcome(isSuccess, result);
        } else if (exchangeId == mLoginXId) {
            handleLoginOutcome(isSuccess, result);
        }
    }


    private void handleLoginOutcome(boolean isSuccess, ForgeExchangeResult result) {
        if (!mAbortLogin) {
            if (isSuccess) {
                int code = result.getCode();

                if (code > 0) {
                    if (code == BasicResponseCodes.Oks.OK.getCode()) {
                        try {
                            JSONObject jobj = new JSONObject(result.getPayload());
                            JSONObject sessionInfo = jobj.optJSONObject("session_info");
                            if (sessionInfo != null) {
                                int sessionTtl = jobj.getInt("session_ttl");

                                mSession.startSession(sessionTtl);

                                mCurrentUserHolder.setCurrentUser(new CurrentUser(sessionInfo.getLong("user_id"),
                                        sessionInfo.getString("screen_name")));

                                mLogger.debug("App login OK");
                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                mAppConfiguration.getAppPrefs().save();

                                switchToCompletedStateSuccess();
                            } else {
                                mLogger.error("Missing session info");
                                mLoginError = LoginError.FAILED;
                                switchToCompletedStateFail();
                            }
                        } catch (JSONException e) {
                            mLogger.warn("Login exchange failed because cannot parse JSON");
                            mLoginError = LoginError.FAILED;
                            switchToCompletedStateFail();
                        }
                    } else {
                        // unexpected positive code
                        mLoginError = LoginError.FAILED;
                        switchToCompletedStateFail();
                    }
                } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    mLoginError = LoginError.UPGRADE_NEEDED;
                    switchToCompletedStateFail();
                } else if (code == AuthorizationResponseCodes.Errors.INVALID_LOGIN.getCode()) {
                    mLoginError = LoginError.INVALID_LOGIN;
                    switchToCompletedStateFail();
                } else {
                    mLoginError = LoginError.FAILED;
                    switchToCompletedStateFail();
                }
            } else {
                mLoginError = LoginError.FAILED;
                switchToCompletedStateFail();
            }
        }
    }


    @Override
    public synchronized void completedStateAcknowledged() {
        super.completedStateAcknowledged();

        mJustAutoregistered = false;
        switchToIdleState();
    }
}
