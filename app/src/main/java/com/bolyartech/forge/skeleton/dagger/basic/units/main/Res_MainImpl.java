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
public class Res_MainImpl extends AbstractMultiOperationResidentComponent<Res_Main.Operation> implements Res_Main,
        ForgeExchangeManagerListener {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;
    private final NetworkInfoProvider mNetworkInfoProvider;

    private boolean mJustAutoregistered = false;

    private volatile long mAutoRegisterXId;
    private volatile long mLoginXId;
    private volatile boolean mAbortLogin = false;

    private LoginResult mLoginResult;
    private AutoregisteringResult mAutoregisteringResult;
    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;


    @Inject
    public Res_MainImpl(AppConfiguration appConfiguration,
                        ForgeExchangeHelper forgeExchangeHelper,
                        Session session,
                        NetworkInfoProvider networkInfoProvider) {


        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
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
    public LoginResult getLoginResult() {
        return mLoginResult;
    }


    @Override
    public AutoregisteringResult getAutoregisteringResult() {
        return mAutoregisteringResult;
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
                        mSession.startSession(sessionTtl, new Session.Info(sessionInfo.getLong("user_id"),
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
                            mAutoregisteringResult = AutoregisteringResult.OK;
                            switchToCompletedState();
                        }
                    } else {
                        mLogger.error("Missing session info");
                        mAutoregisteringResult = AutoregisteringResult.FAILED;
                        switchToCompletedState();
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    mAutoregisteringResult = AutoregisteringResult.FAILED;
                    switchToCompletedState();

                }
            } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                mLogger.warn("Upgrade needed");
                mAutoregisteringResult = AutoregisteringResult.UPGRADE_NEEDED;
                switchToCompletedState();
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                mAutoregisteringResult = AutoregisteringResult.FAILED;
                switchToCompletedState();
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            mAutoregisteringResult = AutoregisteringResult.FAILED;
            switchToCompletedState();
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
                                mSession.startSession(sessionTtl, new Session.Info(sessionInfo.getLong("user_id"),
                                        sessionInfo.getString("screen_name")));
                                mLogger.debug("App login OK");
                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                mAppConfiguration.getAppPrefs().save();

                                mLoginResult = LoginResult.OK;
                                switchToCompletedState();
                            } else {
                                mLogger.error("Missing session info");
                                mLoginResult = LoginResult.FAILED;
                                switchToCompletedState();
                            }
                        } catch (JSONException e) {
                            mLogger.warn("Login exchange failed because cannot parse JSON");
                            mLoginResult = LoginResult.FAILED;
                            switchToCompletedState();
                        }
                    } else {
                        // unexpected positive code
                        mLoginResult = LoginResult.FAILED;
                        switchToCompletedState();
                    }
                } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    mLoginResult = LoginResult.UPGRADE_NEEDED;
                    switchToCompletedState();
                } else if (code == AuthorizationResponseCodes.Errors.INVALID_LOGIN.getCode()) {
                    mLoginResult = LoginResult.INVALID_LOGIN;
                    switchToCompletedState();
                } else {
                    mLoginResult = LoginResult.FAILED;
                    switchToCompletedState();
                }
            } else {
                mLoginResult = LoginResult.FAILED;
                switchToCompletedState();
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
