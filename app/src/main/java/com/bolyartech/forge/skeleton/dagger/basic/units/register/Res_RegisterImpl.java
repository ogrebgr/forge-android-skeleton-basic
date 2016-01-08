package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.content.Context;

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
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForApplication;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2016-01-01 14:37
 */
public class Res_RegisterImpl extends SessionResidentComponent implements Res_Register, ExchangeFunctionality.Listener<ForgeExchangeResult> {
    private StateManager mStateManager = new StateManager();

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final String mAppVersion;

    private final AppPrefs mAppPrefs;

    private final LoginPrefs mLoginPrefs;

    private final Context mAppContext;

    private long mRegisterXId;

    private ResponseCodes.Errors mLastError;


    @Inject
    public Res_RegisterImpl(@Named("app version") String appVersion,
                            AppPrefs appPrefs,
                            LoginPrefs loginPrefs,
                            @ForApplication Context appContext) {

        mAppVersion = appVersion;
        mAppPrefs = appPrefs;
        mLoginPrefs = loginPrefs;
        mAppContext = appContext;
    }


    @Override
    public void register(String username, String password, String screenName) {
        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.REGISTERING);

            ForgeExchangeBuilder b = createForgeExchangeBuilder("register.php");

            b.addPostParameter("username", username);
            b.addPostParameter("password", password);
            b.addPostParameter("screen_name", screenName);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppVersion);

            ForgeExchangeManager em = getForgeExchangeManager();
            mRegisterXId = em.generateXId();
            em.executeExchange(b.build(), mRegisterXId);
        } else {
            mLogger.error("register() called not in IDLE state. Ignoring.");
        }
    }


    @Override
    public State getState() {
        return mStateManager.getState();
    }


    @Override
    public void resetState() {
        mStateManager.switchToState(State.IDLE);
    }


    @Override
    public ResponseCodes.Errors getLastError() {
        return mLastError;
    }


    @Override
    public void onExchangeCompleted(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (mRegisterXId == exchangeId) {
            mLastError = null;
            if (!outcome.isError()) {
                ForgeExchangeResult rez = outcome.getResult();
                int code = rez.getCode();

                if (code == ResponseCodes.Oks.REGISTER_OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(rez.getPayload());
                        int sessionTtl = jobj.getInt("session_ttl");
                        getSession().setSessionTTl(sessionTtl);

                        getSession().setIsLoggedIn(true);
                        mAppPrefs.setLastSuccessfulLoginMethod(LoginMethod.APP);
                        mAppPrefs.setUserId(jobj.getLong("user_id"));
                        mAppPrefs.save();

                        mLogger.debug("App register OK");
                        mStateManager.switchToState(State.REGISTER_OK);
                    } catch (JSONException e) {
                        mLogger.warn("Register exchange failed because cannot parse JSON");
                        mStateManager.switchToState(State.REGISTER_FAIL);
                    }
                } else {
                    mLastError = ResponseCodes.Errors.fromInt(code);
                    mLogger.warn("Register exchange failed with code {}", code);
                    mStateManager.switchToState(State.REGISTER_FAIL);
                }
            } else {
                mLogger.warn("Register exchange failed");
                mStateManager.switchToState(State.REGISTER_FAIL);
            }
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
}


