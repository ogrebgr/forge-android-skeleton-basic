package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.StateManager;
import com.bolyartech.forge.android.app_unit.StateManagerImpl;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.ForgeExchangeHelper;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Mod_MainImpl implements Mod_Main {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final StateManager<Mod_Main.State> mStateManager;

    private final AppConfiguration mAppConfiguration;

    private final Session mSession;

    private final NetworkInfoProvider mNetworkInfoProvider;

    private final ForgeExchangeHelper mForgeExchangeHelper;

    private boolean mJustAutoregistered = false;

    private long mAutoRegisterXId;
    private long mLoginXId;
    private volatile boolean mAbortLogin = false;


    @Inject
    public Mod_MainImpl(
                        AppConfiguration appConfiguration,
                        NetworkInfoProvider networkInfoProvider,
                        AndroidEventPoster androidEventPoster,
                        Session session,
                        ForgeExchangeHelper forgeExchangeHelper
                        ) {

        mAppConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        mStateManager = new StateManagerImpl<>(androidEventPoster, Mod_Main.State.IDLE);
        mSession = session;
        mForgeExchangeHelper = forgeExchangeHelper;
    }


    @Override
    public Mod_Main.State getState() {
        return mStateManager.getState();
    }


    private void init() {
        if (mNetworkInfoProvider.isConnected()) {
            mStateManager.switchToState(Mod_Main.State.IDLE);
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
        mStateManager.switchToState(Mod_Main.State.AUTO_REGISTERING);
        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("register_auto.php");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        mAutoRegisterXId = mForgeExchangeHelper.getExchangeManager().generateTaskId();
        mForgeExchangeHelper.getExchangeManager().executeExchange(b.build(), mAutoRegisterXId);
    }


    @Override
    public void login() {
        loginActual();
    }


    private void loginActual() {
        mStateManager.switchToState(Mod_Main.State.LOGGING_IN);

        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login.php");
        b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
        b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        mLoginXId = mForgeExchangeHelper.getExchangeManager().generateTaskId();
        mForgeExchangeHelper.getExchangeManager().executeExchange(b.build(), mLoginXId);
    }


    @Override
    public void startSession() {
        // here is the place to initiate additional exchanges that retrieve app state/messages/etc
        mStateManager.switchToState(Mod_Main.State.SESSION_STARTED_OK);
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        mStateManager.switchToState(Mod_Main.State.IDLE);
    }


    @Override
    public void logout() {
        mSession.logout();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("logout.php");
                mForgeExchangeHelper.getExchangeManager().executeExchange(b.build(), mForgeExchangeHelper.getExchangeManager().generateTaskId());
            }
        });
        t.start();
        mStateManager.switchToState(Mod_Main.State.IDLE);
    }


    @Override
    public void internetAvailable() {

    }


    @Override
    public void resetState() {
        mJustAutoregistered = false;
        mStateManager.switchToState(Mod_Main.State.IDLE);
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
                        mSession.startSession(sessionTtl, Session.Info.fromJson(sessionInfo));

                        LoginPrefs lp = mAppConfiguration.getLoginPrefs();
                        lp.setUsername(jobj.getString("username"));
                        lp.setPassword(jobj.getString("password"));
                        lp.setManualRegistration(false);
                        lp.save();

                        mAppConfiguration.getAppPrefs().setSelectedLoginMethod(LoginMethod.APP);
                        mAppConfiguration.getAppPrefs().save();

                        mStateManager.switchToState(Mod_Main.State.STARTING_SESSION);

                        mJustAutoregistered = true;

                        if (mAppConfiguration.shallUseGcm()) {
                            processGcmToken();
                        } else {
                            mStateManager.switchToState(Mod_Main.State.SESSION_STARTED_OK);
                        }
                    } else {
                        mLogger.error("Missing session info");
                        mStateManager.switchToState(Mod_Main.State.SESSION_START_FAIL);
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register auto exchange failed because cannot parse JSON");
                    mStateManager.switchToState(Mod_Main.State.REGISTER_AUTO_FAIL);

                }
            } else if (code == ResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                mLogger.warn("Upgrade needed");
                mStateManager.switchToState(Mod_Main.State.UPGRADE_NEEDED);
            } else {
                mLogger.warn("Register auto exchange failed because returned code is {}", code);
                mStateManager.switchToState(Mod_Main.State.REGISTER_AUTO_FAIL);
            }
        } else {
            mLogger.warn("Register auto exchange failed");
            mStateManager.switchToState(Mod_Main.State.REGISTER_AUTO_FAIL);
        }
    }


    private void processGcmToken() {

    }


    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mAutoRegisterXId) {
            handleAutoRegisterOutcome(exchangeId, isSuccess, result);
        } else if (exchangeId == mLoginXId) {
            handleLoginOutcome(exchangeId, isSuccess, result);
        }
    }


    @Override
    public boolean isManualRegistration() {
        return mAppConfiguration.getLoginPrefs().isManualRegistration();
    }


    @Override
    public boolean isLoggedIn() {
        return mSession.isLoggedIn();
    }


    @Override
    public boolean isOnline() {
        return mNetworkInfoProvider.isConnected();
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
                                mSession.startSession(sessionTtl, Session.Info.fromJson(sessionInfo));
                                mLogger.debug("App login OK");
                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                mAppConfiguration.getAppPrefs().save();

                                startSession();
                            } else {
                                mStateManager.switchToState(Mod_Main.State.LOGIN_FAIL);
                                mLogger.error("Missing session info");
                            }
                        } catch (JSONException e) {
                            mStateManager.switchToState(Mod_Main.State.LOGIN_FAIL);
                            mLogger.warn("Login exchange failed because cannot parse JSON");
                        }
                    } else {
                        // unexpected positive code
                        mStateManager.switchToState(Mod_Main.State.LOGIN_FAIL);
                    }
                } else if (code == ResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                    mStateManager.switchToState(Mod_Main.State.UPGRADE_NEEDED);
                } else if (code == ResponseCodes.Errors.INVALID_LOGIN.getCode()) {
                    mStateManager.switchToState(Mod_Main.State.LOGIN_INVALID);
                } else {
                    mStateManager.switchToState(Mod_Main.State.LOGIN_FAIL);
                }
            } else {
                mStateManager.switchToState(Mod_Main.State.LOGIN_FAIL);
            }
        }
    }


    public static class MainStateManager extends StateManagerImpl<Mod_Main.State> {

        public MainStateManager(AndroidEventPoster poster, State initialState) {
            super(poster, initialState);
        }
    }



}
