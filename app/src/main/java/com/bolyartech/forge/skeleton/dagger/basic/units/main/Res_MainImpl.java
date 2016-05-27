package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.StateManager;
import com.bolyartech.forge.android.app_unit.StateManagerImpl;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgeGetHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.ForgeExchangeHelper;
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
public class Res_MainImpl extends SessionResidentComponent implements Res_Main {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final StateManager<State> mStateManager;

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
                        NetworkInfoProvider networkInfoProvider,
                        AndroidEventPoster androidEventPoster) {

        super(forgeExchangeHelper, session, networkInfoProvider, androidEventPoster);

        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        mStateManager = new StateManagerImpl<>(androidEventPoster, State.IDLE);
    }


    @Override
    public State getState() {
        return mStateManager.getState();
    }


    @Override
    public void onCreate() {
        super.onCreate();

        if (mNetworkInfoProvider.isConnected()) {
            mStateManager.switchToState(State.IDLE);
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
//        if (mStateManager.getParentState() == State.NO_INET) {
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
        mStateManager.switchToState(State.AUTO_REGISTERING);
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
        mStateManager.switchToState(State.LOGGING_IN);

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
        mStateManager.switchToState(State.SESSION_STARTED_OK);
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        mStateManager.switchToState(State.IDLE);
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
        mStateManager.switchToState(State.IDLE);
    }


    @Override
    public void internetAvailable() {

    }


    @Override
    public void resetState() {
        mJustAutoregistered = false;
        mStateManager.switchToState(State.IDLE);
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

                        mStateManager.switchToState(State.STARTING_SESSION);

                        mJustAutoregistered = true;

                        if (mAppConfiguration.shallUseGcm()) {
                            processGcmToken();
                        } else {
                            mStateManager.switchToState(State.SESSION_STARTED_OK);
                        }
                    } else {
                        mLogger.error("Missing session info");
                        mStateManager.switchToState(State.SESSION_START_FAIL);
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    mStateManager.switchToState(State.REGISTER_AUTO_FAIL);

                }
            } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                mLogger.warn("Upgrade needed");
                mStateManager.switchToState(State.UPGRADE_NEEDED);
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                mStateManager.switchToState(State.REGISTER_AUTO_FAIL);
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            mStateManager.switchToState(State.REGISTER_AUTO_FAIL);
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
                                mStateManager.switchToState(State.LOGIN_FAIL);
                                mLogger.error("Missing session info");
                            }
                        } catch (JSONException e) {
                            mStateManager.switchToState(State.LOGIN_FAIL);
                            mLogger.warn("Login exchange failed because cannot parse JSON");
                        }
                    } else {
                        // unexpected positive code
                        mStateManager.switchToState(State.LOGIN_FAIL);
                    }
                } else if (code == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    mStateManager.switchToState(State.UPGRADE_NEEDED);
                } else if (code == BasicResponseCodes.Errors.INVALID_LOGIN.getCode()) {
                    mStateManager.switchToState(State.LOGIN_INVALID);
                } else {
                    mStateManager.switchToState(State.LOGIN_FAIL);
                }
            } else {
                mStateManager.switchToState(State.LOGIN_FAIL);
            }
        }
    }
}
