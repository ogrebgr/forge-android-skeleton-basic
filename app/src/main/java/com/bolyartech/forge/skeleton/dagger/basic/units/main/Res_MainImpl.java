package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Context;
import android.content.Intent;

import com.bolyartech.forge.exchange.ExchangeFunctionality;
import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeManager;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.misc.NetworkInfoProvider;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForApplication;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class Res_MainImpl extends SessionResidentComponent implements Res_Main,
        ExchangeFunctionality.Listener<ForgeExchangeResult> {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    private StateManager mStateManager = new StateManager();

    private final String mAppVersion;

    private final AppPrefs mAppPrefs;

    private final LoginPrefs mLoginPrefs;

    private final Context mAppContext;

    private NetworkInfoProvider mNetworkInfoProvider;


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
//        if (mStateManager.getState() == State.NO_INET) {
//            if (mNetworkInfoProvider.isConnected()) {
//                if (!getSession().isLoggedIn()) {
//                    init();
//                }
//            }
//        }
    }


    private void autoRegister() {
        mStateManager.switchToState(State.AUTO_REGISTERING);
        ForgeExchangeBuilder b = createForgeExchangeBuilder("register_auto.php");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppVersion);

        ForgeExchangeManager em = getForgeExchangeManager();
        mAutoRegisterXId = em.generateXId();
        em.executeExchange(b.build(), mAutoRegisterXId);
    }


    @Override
    public void onExchangeCompleted(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (exchangeId == mAutoRegisterXId) {
            handleAutoRegisterXResult(outcome, exchangeId);
        } else if (exchangeId == mLoginXId) {
            handleLoginXResult(outcome, exchangeId);
        }

//            handleAutoRegisterXResult(out, exchangeId);
//        } else if (mGcmTokenXId.equals(exchangeId)) {
//            handleGcmToken(out, exchangeId);
//        } else if (mFbCheckXId.equals(exchangeId)) {
//            handleCheckFbLogin(out, exchangeId);
//        } else if (mGoogleCheckXId.equals(exchangeId)) {
//            handleGoogleCheckResult(out);
//        }
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

        ForgeExchangeManager em = getForgeExchangeManager();
        mLoginXId = em.generateXId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public void startSession() {
        // here is the place to initiate additional exchanges that retrieve app state/messages/etc
        mStateManager.switchToState(State.SESSION_STARTED_OK);
    }


    @Override
    public boolean isFacebookManualReloginNeeded() {
        return false;
    }


    @Override
    public void clearFacebookManualReloginNeeded() {

    }


    @Override
    public void onFacebookActivityResult(int requestCode, int resultCode, Intent data) {

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
                em.executeExchange(b.build(), em.generateXId());
            }
        });
        t.start();
        mStateManager.switchToState(State.IDLE);
    }


    @Override
    public void internetAvailable() {

    }


    @Override
    public LoginMethod getLastAttemptedLoginMethod() {
        return null;
    }


    @Override
    public boolean isGoogleNativeLoginFail() {
        return false;
    }


    @Override
    public boolean isFacebookNativeLoginFail() {
        return false;
    }


    @Override
    public void resetState() {

    }


    private void handleAutoRegisterXResult(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (!outcome.isError()) {
            ForgeExchangeResult rez = outcome.getResult();
            int code = rez.getCode();

            if (code == ResponseCodes.Oks.REGISTER_AUTO_OK.getCode()) {
                try {
                    JSONObject jobj = new JSONObject(rez.getPayload());
                    mLoginPrefs.setUsername(jobj.getString("username"));
                    mLoginPrefs.setPassword(jobj.getString("password"));
                    mLoginPrefs.setManualRegistration(false);
                    mLoginPrefs.setPublicName(jobj.getString("user_id"));
                    mLoginPrefs.save();
                    mAppPrefs.setSelectedLoginMethod(LoginMethod.APP);
                    mAppPrefs.setUserId(jobj.getLong("user_id"));
                    mAppPrefs.save();

                    int sessionTtl = jobj.getInt("session_ttl");
                    getSession().setSessionTTl(sessionTtl);

                    mStateManager.switchToState(State.STARTING_SESSION);
                    getSession().setIsLoggedIn(true);

                    if (mAppContext.getResources().getBoolean(R.bool.app_conf__use_gcm)) {
                        processGcmToken();
                    } else {
                        mStateManager.switchToState(State.SESSION_STARTED_OK);
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


    private void handleLoginXResult(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (!mAbortLogin) {
            if (!outcome.isError()) {
                ForgeExchangeResult rez = outcome.getResult();
                int code = rez.getCode();

                if (code > 0) {
                    if (code == ResponseCodes.Oks.LOGIN_OK.getCode()) {
                        try {
                            JSONObject jobj = new JSONObject(rez.getPayload());
                            int sessionTtl = jobj.getInt("session_ttl");
                            getSession().setSessionTTl(sessionTtl);

                            mLogger.debug("App login OK");
                            getSession().setIsLoggedIn(true);
                            mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.APP);
                            mAppPrefs.save();

                            startSession();
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
