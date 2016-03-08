package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.StateManager;
import com.bolyartech.forge.android.app_unit.StateManagerImpl;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


public class Res_SelectLoginImpl extends SessionResidentComponent implements Res_SelectLogin {
    private final StateManager<State> mStateManager;

    private long mFacebookCheckXId;
    private long mGoogleCheckXId;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppPrefs mAppPrefs;
    private final String mAppVersion;
    private final LoginPrefs mLoginPrefs;


    @Inject
    public Res_SelectLoginImpl(@Named("app version") String appVersion,
                               AppPrefs appPrefs,
                               LoginPrefs loginPrefs,
                               AndroidEventPoster androidEventPoster) {

        mAppVersion = appVersion;
        mAppPrefs = appPrefs;
        mLoginPrefs = loginPrefs;

        mStateManager = new StateManagerImpl<>(androidEventPoster, State.IDLE);
    }


    @Override
    public State getState() {
        return mStateManager.getState();
    }


    @Override
    public void checkFbLogin(String token, String facebookUserId) {
        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.WAITING_FB_CHECK);
            ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login_fb.php");

            b.addPostParameter("token", token);
            b.addPostParameter("user_id", facebookUserId);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppVersion);
            b.addPostParameter("session_info", "1");

            if (!mLoginPrefs.isManualRegistration()) {
                b.addPostParameter("username", mLoginPrefs.getUsername());
                b.addPostParameter("password", mLoginPrefs.getPassword());
            }

            ForgeExchangeManager em = getForgeExchangeManager();
            mFacebookCheckXId = em.generateTaskId();
            em.executeExchange(b.build(), mFacebookCheckXId);
        } else {
            mLogger.warn("checkFbLogin(): Not in state IDLE");
        }
    }


    @Override
    public void logout() {

    }


    @Override
    public void resetState() {
        mStateManager.reset();
    }


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mFacebookCheckXId) {
            handleFbCheckResult(isSuccess, result);
        } else if (exchangeId == mGoogleCheckXId) {
            handleGoogleCheckResult(isSuccess, result);
        }
    }


//    private class StateManager {
//        private State mState = State.IDLE;
//
//
//        public State getState() {
//            return mState;
//        }
//
//
//        public void switchToState(State state) {
//            mState = state;
//            postEvent(new Ev_StateChanged());
//        }
//
//
//        public void reset() {
//            mState = State.IDLE;
//        }
//
//    }


    private void handleFbCheckResult(boolean isSuccess, ForgeExchangeResult result) {
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

                            mLogger.debug("Facebook login OK");

                            mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.FACEBOOK);
                            mAppPrefs.save();

                            mStateManager.switchToState(State.FB_CHECK_OK);
                        } else {
                            mStateManager.switchToState(State.FB_CHECK_FAIL);
                            mLogger.error("Missing session info");
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Facebook login FAIL. JSON error:", result.getPayload());
                        mStateManager.switchToState(State.FB_CHECK_FAIL);
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    mStateManager.switchToState(State.FB_CHECK_FAIL);
                }
            } else {
                mLogger.debug("Facebook login FAIL. Code: {}", code);
                mStateManager.switchToState(State.FB_CHECK_FAIL);
            }
        } else {
            mLogger.debug("Facebook login FAIL");
            mStateManager.switchToState(State.FB_CHECK_FAIL);
        }
    }


    @Override
    public void checkGoogleLogin(String token) {
        mLogger.debug("Got google token", token);


        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.WAITING_GOOGLE_CHECK);
            ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login_google.php");

            b.addPostParameter("token", token);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppVersion);
            b.addPostParameter("session_info", "1");

            if (!mLoginPrefs.isManualRegistration()) {
                b.addPostParameter("username", mLoginPrefs.getUsername());
                b.addPostParameter("password", mLoginPrefs.getPassword());
            }

            ForgeExchangeManager em = getForgeExchangeManager();
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
                if (code == ResponseCodes.Oks.LOGIN_OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));


                            mLogger.debug("Google login OK");

                            mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.GOOGLE);
                            mAppPrefs.save();

                            mStateManager.switchToState(State.GOOGLE_CHECK_OK);
                        } else {
                            mStateManager.switchToState(State.GOOGLE_CHECK_FAIL);
                            mLogger.error("Missing session info");
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Google login FAIL. JSON error:", result.getPayload());
                        mStateManager.switchToState(State.GOOGLE_CHECK_FAIL);
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    mStateManager.switchToState(State.GOOGLE_CHECK_FAIL);
                }
            } else {
                mLogger.debug("Facebook login FAIL. Code: {}", code);
                mStateManager.switchToState(State.GOOGLE_CHECK_FAIL);
            }
        } else {
            mLogger.debug("Facebook login FAIL");
            mStateManager.switchToState(State.GOOGLE_CHECK_FAIL);
        }
    }
}
