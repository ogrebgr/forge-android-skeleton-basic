package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.misc.StringUtils;
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
 * Created by ogre on 2016-01-01 14:37
 */
public class Res_RegisterImpl extends SessionResidentComponent<Res_Register.State> implements Res_Register {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    private volatile long mRegisterXId;
    private volatile long mPostAutoRegisterXId;

    private BasicResponseCodes.Errors mLastError;
    private String mLastUsedUsername;
    private String mLastUsedPassword;

    private final AppConfiguration mAppConfiguration;


    @Inject
    public Res_RegisterImpl(AppConfiguration appConfiguration,
                            ForgeExchangeHelper forgeExchangeHelper,
                            Session session,
                            NetworkInfoProvider networkInfoProvider) {

        super(State.IDLE, forgeExchangeHelper, session, networkInfoProvider);

        mAppConfiguration = appConfiguration;
    }


    @Override
    public void register(String username, String password, String screenName) {
        if (getState() == State.IDLE) {
            switchToState(State.REGISTERING);

            mLastUsedUsername = username;
            mLastUsedPassword = password;

            if (StringUtils.isEmpty(mAppConfiguration.getLoginPrefs().getUsername())) {
                normalRegistration(username, password, screenName);
            } else {
                if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                    postAutoRegistration(username, password, screenName);
                } else {
                    // register() method should not been called in this condition
                    switchToState(State.REGISTER_FAIL);
                }
            }
        } else {
            mLogger.error("register() called not in IDLE state. Ignoring.");
        }
    }


    private void postAutoRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("register_postauto");

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
        mPostAutoRegisterXId = em.generateTaskId();
        mLogger.debug("mPostAutoRegisterXId {}", mPostAutoRegisterXId);
        em.executeExchange(b.build(), mPostAutoRegisterXId);
    }


    private void normalRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("register");

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
    public void stateHandled() {
        if (isInOneOfStates(State.REGISTER_FAIL, State.REGISTER_OK)) {
            resetState();
        }
    }


    @Override
    public BasicResponseCodes.Errors getLastError() {
        return mLastError;
    }


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mRegisterXId == exchangeId) {
            if (handleRegistrationCommon1(isSuccess, result)) {
                try {
                    JSONObject jobj = new JSONObject(result.getPayload());
                    int sessionTtl = jobj.getInt("session_ttl");

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));
                        handleRegistrationCommon2();
                    } else {
                        mLogger.error("Missing session info");
                        switchToState(State.REGISTER_FAIL);
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register exchange failed because cannot parse JSON");
                    switchToState(State.REGISTER_FAIL);
                }
            }
        } else if (mPostAutoRegisterXId == exchangeId)  {
            if (handleRegistrationCommon1(isSuccess, result)) {
                handleRegistrationCommon2();
            }
        }
    }


    private boolean handleRegistrationCommon1(boolean isSuccess, ForgeExchangeResult result) {
        mLastError = null;
        if (!isSuccess) {
            mLogger.warn("Register exchange failed");
            switchToState(State.REGISTER_FAIL);
            return false;
        }

        int code = result.getCode();

        if (code != BasicResponseCodes.Oks.OK.getCode()) {
            mLastError = BasicResponseCodes.Errors.fromInt(code);
            mLogger.warn("Register exchange failed with code {}", code);
            switchToState(State.REGISTER_FAIL);
            return false;
        }

        return true;
    }


    private void handleRegistrationCommon2() {
        mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
        mAppConfiguration.getAppPrefs().save();

        LoginPrefs lp = mAppConfiguration.getLoginPrefs();
        lp.setUsername(mLastUsedUsername);
        lp.setPassword(mLastUsedPassword);
        lp.setManualRegistration(true);
        lp.save();

        mLogger.debug("App register OK");
        switchToState(State.REGISTER_OK);
    }

}


