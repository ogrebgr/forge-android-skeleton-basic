package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Context;
import android.content.Intent;

import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForApplication;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;
import com.bolyartech.forge.task.ForgeExchangeManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class Res_MainImpl extends SessionResidentComponent implements Res_Main {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    private StateManager mStateManager = new StateManager();

    private final String mAppVersion;

    private final AppPrefs mAppPrefs;

    private final LoginPrefs mLoginPrefs;

    private final Context mAppContext;

    private NetworkInfoProvider mNetworkInfoProvider;

    private boolean mJustAutoregistered = false;

    private long mAutoRegisterXId;
    private long mLoginXId;
    private volatile boolean mAbortLogin = false;


    @Inject
    public Res_MainImpl(@Named("app version") String appVersion,
                        AppPrefs appPrefs,
                        LoginPrefs loginPrefs,
                        @ForApplication Context appContext,
                        NetworkInfoProvider networkInfoProvider) {

        mAppVersion = appVersion;
        mAppPrefs = appPrefs;
        mLoginPrefs = loginPrefs;
        mAppContext = appContext;
        mNetworkInfoProvider = networkInfoProvider;
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
            if (mLoginPrefs.hasLoginCredentials()) {
                if (mAppPrefs.getSelectedLoginMethod() != null) {
                    loginActual();
                }
            } else {
                if (mAppContext.getResources().getBoolean(R.bool.app_conf__do_autoregister)) {
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
        ForgeExchangeBuilder b = createForgeExchangeBuilder("register_auto.php");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppVersion);
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
        mStateManager.switchToState(State.LOGGING_IN);

        ForgeExchangeBuilder b = createForgeExchangeBuilder("login.php");
        b.addPostParameter("username", mLoginPrefs.getUsername());
        b.addPostParameter("password", mLoginPrefs.getPassword());
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppVersion);
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
                ForgeExchangeBuilder b = createForgeExchangeBuilder("logout.php");
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


    private void handleAutoRegisterOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();

            if (code == ResponseCodes.Oks.REGISTER_AUTO_OK.getCode()) {
                try {
                    JSONObject jobj = new JSONObject(result.getPayload());

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        int sessionTtl = jobj.getInt("session_ttl");
                        getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));

                        mLoginPrefs.setUsername(jobj.getString("username"));
                        mLoginPrefs.setPassword(jobj.getString("password"));
                        mLoginPrefs.setManualRegistration(false);
                        mLoginPrefs.save();

                        mAppPrefs.setSelectedLoginMethod(LoginMethod.APP);
                        mAppPrefs.save();

                        mStateManager.switchToState(State.STARTING_SESSION);

                        mJustAutoregistered = true;

                        if (mAppContext.getResources().getBoolean(R.bool.app_conf__use_gcm)) {
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
            } else if (code == ResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
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
            handleAutoRegisterOutcome(exchangeId, isSuccess, result);
        } else if (exchangeId == mLoginXId) {
            handleLoginOutcome(exchangeId, isSuccess, result);
        }
    }


    private class StateManager {
        private State mState = State.IDLE;


        public State getState() {
            return mState;
        }


        public void switchToState(State state) {
            mState = state;
            postEvent(new Ev_StateChanged());
        }
    }


    private void handleLoginOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (!mAbortLogin) {
            if (isSuccess) {
                int code = result.getCode();

                if (code > 0) {
                    if (code == ResponseCodes.Oks.LOGIN_OK.getCode()) {
                        try {
                            JSONObject jobj = new JSONObject(result.getPayload());
                            JSONObject sessionInfo = jobj.optJSONObject("session_info");
                            if (sessionInfo != null) {
                                int sessionTtl = jobj.getInt("session_ttl");
                                getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));
                                mLogger.debug("App login OK");
                                mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.APP);
                                mAppPrefs.save();

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
                } else if (code == ResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    mStateManager.switchToState(State.UPGRADE_NEEDED);
                } else if (code == ResponseCodes.Errors.INVALID_LOGIN.getCode()) {
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
