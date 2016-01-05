package com.bolyartech.forge.skeleton.dagger.basic.units.login;

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
 * Created by ogre on 2016-01-05 14:26
 */
public class Res_LoginImpl extends SessionResidentComponent implements Res_Login, ExchangeFunctionality.Listener<ForgeExchangeResult> {
    private final LoginPrefs mLoginPrefs;
    private final String mAppVersion;
    private final AppPrefs mAppPrefs;

    private final StateManager mStateManager = new StateManager();
    private ResponseCodes.Errors mLastError;

    private long mLoginXId;
    private volatile boolean mAbortLogin = false;


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    @Inject
    public Res_LoginImpl(@Named("app version") String appVersion,
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
    public void login(String username, String password) {
        mStateManager.switchToState(State.LOGGING_IN);

        ForgeExchangeBuilder b = createForgeExchangeBuilder("login.php");
        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppVersion);

        ForgeExchangeManager em = getForgeExchangeManager();
        mLoginXId = em.generateXId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public ResponseCodes.Errors getLastError() {
        return mLastError;
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        mStateManager.switchToState(State.IDLE);
    }


    @Override
    public void onExchangeCompleted(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (exchangeId == mLoginXId) {
            mLastError = null;
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
                    } else {
                        mLogger.warn("Login exchange failed with code {}", code);
                        mLastError = ResponseCodes.Errors.fromInt(code);
                        mStateManager.switchToState(State.LOGIN_FAIL);
                    }
                } else {
                    mStateManager.switchToState(State.LOGIN_FAIL);
                }
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


    private void startSession() {
        // here is the place to initiate additional exchanges that retrieve app state/messages/etc
        mStateManager.switchToState(State.SESSION_STARTED_OK);
    }

}
