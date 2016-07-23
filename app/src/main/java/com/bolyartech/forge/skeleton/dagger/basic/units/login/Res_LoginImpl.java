package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.SimpleStateManagerImpl;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * Created by ogre on 2016-01-05 14:26
 */
public class Res_LoginImpl extends SessionResidentComponent<Res_Login.State> implements Res_Login {
    private BasicResponseCodes.Errors mLastError;

    private volatile long mLoginXId;
    private volatile boolean mAbortLogin = false;

    private String mLastUsedUsername;
    private String mLastUsedPassword;


    private final AppConfiguration mAppConfiguration;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    @Inject
    public Res_LoginImpl(
            AppConfiguration appConfiguration,
            ForgeExchangeHelper forgeExchangeHelper,
            Session session,
            NetworkInfoProvider networkInfoProvider,
            Bus bus) {

        super(new SimpleStateManagerImpl<>(bus, State.IDLE), forgeExchangeHelper, session, networkInfoProvider);

        mAppConfiguration = appConfiguration;
    }


    @Override
    public void login(String username, String password) {
        switchToState(State.LOGGING_IN);
        mLastUsedUsername = username;
        mLastUsedPassword = password;

        ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login");
        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());

        ForgeExchangeManager em = getForgeExchangeManager();
        mLoginXId = em.generateTaskId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public BasicResponseCodes.Errors getLastError() {
        return mLastError;
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        switchToState(State.IDLE);
    }


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mLoginXId) {
            mLastError = null;
            if (!mAbortLogin) {
                if (isSuccess) {
                    int code = result.getCode();

                    if (code > 0) {
                        if (code == BasicResponseCodes.Oks.OK.getCode()) {
                            try {
                                JSONObject jobj = new JSONObject(result.getPayload());
                                int sessionTtl = jobj.getInt("session_ttl");
                                JSONObject sessionInfo = jobj.optJSONObject("session_info");
                                if (sessionInfo != null) {
                                    getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));
                                    mLogger.debug("App login OK");

                                    mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                    mAppConfiguration.getAppPrefs().save();

                                    LoginPrefs lp = mAppConfiguration.getLoginPrefs();
                                    lp.setUsername(mLastUsedUsername);
                                    lp.setPassword(mLastUsedPassword);
                                    lp.setManualRegistration(true);
                                    lp.save();

                                    startSession();
                                } else {
                                    mLogger.error("Missing session info");
                                    switchToState(State.LOGIN_FAIL);

                                }
                            } catch (JSONException e) {
                                mLogger.warn("Login exchange failed because cannot parse JSON");
                                switchToState(State.LOGIN_FAIL);
                            }
                        } else {
                            // unexpected positive code
                            switchToState(State.LOGIN_FAIL);
                        }
                    } else {
                        mLogger.warn("Login exchange failed with code {}", code);
                        mLastError = BasicResponseCodes.Errors.fromInt(code);
                        switchToState(State.LOGIN_FAIL);
                    }
                } else {
                    switchToState(State.LOGIN_FAIL);
                }
            }
        }
    }


    private void startSession() {
        // here is the place to initiate additional exchanges that retrieve app state/messages/etc
        switchToState(State.SESSION_STARTED_OK);
    }


    @Override
    public void stateHandled() {
        if (isInOneOfStates(State.LOGIN_FAIL, State.SESSION_START_FAIL, State.SESSION_STARTED_OK)) {
            resetState();
        }
    }
}
