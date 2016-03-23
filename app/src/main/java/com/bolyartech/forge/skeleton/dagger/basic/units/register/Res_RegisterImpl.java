package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.StateManager;
import com.bolyartech.forge.android.app_unit.StateManagerImpl;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ForgeExchangeHelper;
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


/**
 * Created by ogre on 2016-01-01 14:37
 */
public class Res_RegisterImpl extends SessionResidentComponent implements Res_Register {
    private final StateManager<State> mStateManager;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    private long mRegisterXId;

    private ResponseCodes.Errors mLastError;
    private String mLastUsedUsername;
    private String mLastUsedPassword;

    private final AppConfiguration mAppConfiguration;


    @Inject
    public Res_RegisterImpl(AppConfiguration appConfiguration,
                            ForgeExchangeHelper forgeExchangeHelper,
                            Session session,
                            NetworkInfoProvider networkInfoProvider,
                            AndroidEventPoster androidEventPoster) {

        super(appConfiguration, forgeExchangeHelper, session, networkInfoProvider, androidEventPoster);
        mStateManager = new StateManagerImpl<>(androidEventPoster, State.IDLE);

        mAppConfiguration = appConfiguration;
    }


    @Override
    public void register(String username, String password, String screenName) {
        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.REGISTERING);

            mLastUsedUsername = username;
            mLastUsedPassword = password;

            if (StringUtils.isEmpty(mAppConfiguration.getLoginPrefs().getUsername())) {
                normalRegistration(username, password, screenName);
            } else {
                if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                    postAutoRegistration(username, password, screenName);
                } else {
                    // register() method should not been called in this condition
                    mStateManager.switchToState(State.REGISTER_FAIL);
                }
            }
        } else {
            mLogger.error("register() called not in IDLE state. Ignoring.");
        }
    }


    private void postAutoRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("register_postauto.php");

        LoginPrefs lp = mAppConfiguration.getLoginPrefs();
        b.addPostParameter("username", lp.getUsername());
        b.addPostParameter("password", lp.getPassword());
        b.addPostParameter("new_username", username);
        b.addPostParameter("new_password", password);
        b.addPostParameter("screen_name", screenName);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");
        b.addPostParameter("do_login", "1");

        ForgeExchangeManager em = getForgeExchangeManager();
        mRegisterXId = em.generateTaskId();
        em.executeExchange(b.build(), mRegisterXId);
    }


    private void normalRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("register.php");

        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("screen_name", screenName);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");
        b.addPostParameter("do_login", "1");

        ForgeExchangeManager em = getForgeExchangeManager();
        mRegisterXId = em.generateTaskId();
        em.executeExchange(b.build(), mRegisterXId);
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
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mRegisterXId == exchangeId) {
            mLastError = null;
            if (isSuccess) {
                int code = result.getCode();

                if (code == ResponseCodes.Oks.REGISTER_OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        int sessionTtl = jobj.getInt("session_ttl");

                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                            mAppConfiguration.getAppPrefs().save();

                            LoginPrefs lp = mAppConfiguration.getLoginPrefs();
                            lp.setUsername(mLastUsedUsername);
                            lp.setPassword(mLastUsedPassword);
                            lp.setManualRegistration(true);
                            lp.save();

                            mLogger.debug("App register OK");
                            mStateManager.switchToState(State.REGISTER_OK);
                        } else {
                            mLogger.error("Missing session info");
                            mStateManager.switchToState(State.REGISTER_FAIL);
                        }
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
}


