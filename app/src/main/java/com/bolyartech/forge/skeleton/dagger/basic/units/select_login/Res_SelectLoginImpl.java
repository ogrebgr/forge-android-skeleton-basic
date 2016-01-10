package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.exchange.ExchangeFunctionality;
import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeManager;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2016-01-07 09:05
 */
public class Res_SelectLoginImpl extends SessionResidentComponent implements Res_SelectLogin, ExchangeFunctionality.Listener<ForgeExchangeResult> {
    private final StateManager mStateManager = new StateManager();

    private long mFacebookCheckXId;
    private long mGoogleCheckXId;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppPrefs mAppPrefs;
    private final String mAppVersion;
    private final LoginPrefs mLoginPrefs;

    @Inject
    public Res_SelectLoginImpl(@Named("app version") String appVersion,
                               AppPrefs appPrefs,
                               LoginPrefs loginPrefs) {

        mAppVersion = appVersion;
        mAppPrefs = appPrefs;
        mLoginPrefs = loginPrefs;
    }


    @Override
    public State getState() {
        return mStateManager.getState();
    }


    @Override
    public void checkFbLogin(String token, String facebookUserId) {
        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.WAITING_FB_CHECK);
            ForgeExchangeBuilder b = createForgeExchangeBuilder("login_fb.php");

            b.addPostParameter("token", token);
            b.addPostParameter("user_id", facebookUserId);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppVersion);

            if (!mLoginPrefs.isManualRegistration() && mAppPrefs.getUserId() > 0) {
                b.addPostParameter("username", mLoginPrefs.getUsername());
                b.addPostParameter("password", mLoginPrefs.getPassword());
            }

            ForgeExchangeManager em = getForgeExchangeManager();
            mFacebookCheckXId = em.generateXId();
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
    public void onExchangeCompleted(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (exchangeId == mFacebookCheckXId) {
            handleFbCheckResult(outcome, exchangeId);
        } else if (exchangeId == mGoogleCheckXId) {

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


        public void reset() {
            mState = State.IDLE;
        }

    }


    private void handleFbCheckResult(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (!outcome.isError()) {
            ForgeExchangeResult rez = outcome.getResult();
            int code = rez.getCode();
            if (code > 0) {
                if (code == ResponseCodes.Oks.LOGIN_OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(rez.getPayload());
                        int sessionTtl = jobj.getInt("session_ttl");
                        getSession().setSessionTTl(sessionTtl);

                        mLogger.debug("Facebook login OK");
                        getSession().setIsLoggedIn(true);

                        mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.FACEBOOK);
                        mAppPrefs.save();

                        mStateManager.switchToState(State.FB_CHECK_OK);
                    } catch (JSONException e) {
                        mLogger.debug("Facebook login FAIL. JSON error:", rez.getPayload());
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

//
//        mStateManager.switchToState(State.WAITING_GOOGLE_CHECK);
//        RestExchangeBuilder<KhRestResult> b = mKhRestExchangeBuilderFactory
//                .createRestExchangeBuilder("login_google.php");
//
//        b.addPostParameter("token", token);
//        b.addPostParameter("app_key", mAppKey);
//        b.addPostParameter("app_type", "1");
//        b.addPostParameter("app_version", mAppVersion);
//
//        b.addPostParameter("username", mGgPrefs.getUsername());
//        b.addPostParameter("password", mGgPrefs.getPassword());
//
//
//        mGoogleCheckXId = getKhRestExchangeManager().reserveXId();
//        getKhRestExchangeManager().executeKhRestExchange(b.build(), mGoogleCheckXId);
    }

}
