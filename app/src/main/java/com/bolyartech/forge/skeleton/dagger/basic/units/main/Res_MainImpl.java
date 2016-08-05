package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgeGetHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class Res_MainImpl  extends SessionResidentComponent<Res_Main.State> implements Res_Main {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;
    private final NetworkInfoProvider mNetworkInfoProvider;

    private boolean mJustAutoregistered = false;

    private volatile long mAutoRegisterXId;
    private volatile long mLoginXId;
    private volatile boolean mAbortLogin = false;


    @Inject
    public Res_MainImpl(AppConfiguration appConfiguration,
                        ForgeExchangeHelper forgeExchangeHelper,
                        Session session,
                        NetworkInfoProvider networkInfoProvider) {

        super(State.IDLE, forgeExchangeHelper, session, networkInfoProvider);

        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        if (mNetworkInfoProvider.isConnected()) {
            switchToState(State.IDLE);
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
    public boolean isJustAutoregistered() {
        return mJustAutoregistered;
    }


    private void autoRegister() {
        switchToState(State.AUTO_REGISTERING);
        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("autoregister");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        ForgeExchangeManager em = getForgeExchangeManager();
        mAutoRegisterXId = em.generateTaskId();
        em.executeExchange(b.build(), mAutoRegisterXId);
    }


    @Override
    public void login() {
        loginActual();
    }


    private void loginActual() {
        mAbortLogin = false;
        switchToState(State.LOGGING_IN);

        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login");
        b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
        b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        ForgeExchangeManager em = getForgeExchangeManager();
        mLoginXId = em.generateTaskId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public void startSession() {
        // here is the place to initiate additional exchanges that retrieve app state/messages/etc
        switchToState(State.SESSION_STARTED_OK);
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        switchToState(State.IDLE);
    }


    @Override
    public void logout() {
        getSession().logout();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ForgeGetHttpExchangeBuilder b = createForgeGetHttpExchangeBuilder("logout");
                ForgeExchangeManager em = getForgeExchangeManager();
                em.executeExchange(b.build(), em.generateTaskId());
            }
        });
        t.start();
        switchToState(State.IDLE);
    }


    @Override
    public void internetAvailable() {

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
                        getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));

                        LoginPrefs lp = mAppConfiguration.getLoginPrefs();

                        lp.setUsername(jobj.getString("username"));
                        lp.setPassword(jobj.getString("password"));
                        lp.setManualRegistration(false);
                        lp.save();

                        mAppConfiguration.getAppPrefs().setSelectedLoginMethod(LoginMethod.APP);
                        mAppConfiguration.getAppPrefs().save();

                        switchToState(State.STARTING_SESSION);

                        mJustAutoregistered = true;

                        if (mAppConfiguration.shallUseGcm()) {
                            processGcmToken();
                        } else {
                            switchToState(State.SESSION_STARTED_OK);
                        }
                    } else {
                        mLogger.error("Missing session info");
                        switchToState(State.SESSION_START_FAIL);
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    switchToState(State.REGISTER_AUTO_FAIL);

                }
            } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                mLogger.warn("Upgrade needed");
                switchToState(State.UPGRADE_NEEDED);
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                switchToState(State.REGISTER_AUTO_FAIL);
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            switchToState(State.REGISTER_AUTO_FAIL);
        }
    }


    private void processGcmToken() {

    }


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
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
                                getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));
                                mLogger.debug("App login OK");
                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                mAppConfiguration.getAppPrefs().save();

                                startSession();
                            } else {
                                switchToState(State.LOGIN_FAIL);
                                mLogger.error("Missing session info");
                            }
                        } catch (JSONException e) {
                            switchToState(State.LOGIN_FAIL);
                            mLogger.warn("Login exchange failed because cannot parse JSON");
                        }
                    } else {
                        // unexpected positive code
                        switchToState(State.LOGIN_FAIL);
                    }
                } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    switchToState(State.UPGRADE_NEEDED);
                } else if (code == BasicResponseCodes.Errors.INVALID_LOGIN.getCode()) {
                    switchToState(State.LOGIN_INVALID);
                } else {
                    switchToState(State.LOGIN_FAIL);
                }
            } else {
                switchToState(State.LOGIN_FAIL);
            }
        }
    }


    @Override
    public void stateHandled() {
        if (isInOneOfStates(State.LOGIN_FAIL,
                State.LOGIN_INVALID,
                State.REGISTER_AUTO_FAIL,
                State.SESSION_START_FAIL,
                State.SESSION_STARTED_OK
        )) {

            mJustAutoregistered = false;
            resetState();
        }
    }
}
