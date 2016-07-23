package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.SimpleStateManagerImpl;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;
import com.squareup.otto.Bus;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Res_SelectLoginImpl extends SessionResidentComponent<Res_SelectLogin.State> implements Res_SelectLogin {
    private volatile long mFacebookCheckXId;
    private volatile long mGoogleCheckXId;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;


    @Inject
    public Res_SelectLoginImpl(AppConfiguration appConfiguration,
                               ForgeExchangeHelper forgeExchangeHelper,
                               Session session,
                               NetworkInfoProvider networkInfoProvider,
                               Bus bus) {

        super(new SimpleStateManagerImpl<>(bus, State.IDLE), forgeExchangeHelper, session, networkInfoProvider);

        mAppConfiguration = appConfiguration;
    }




    @Override
    public void checkFbLogin(String token, String facebookUserId) {
        if (getState() == State.IDLE) {
            switchToState(State.WAITING_FB_CHECK);
            ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login_fb.php");

            b.addPostParameter("token", token);
            b.addPostParameter("user_id", facebookUserId);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
            b.addPostParameter("session_info", "1");

            if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
                b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
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
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mFacebookCheckXId) {
            handleFbCheckResult(isSuccess, result);
        } else if (exchangeId == mGoogleCheckXId) {
            handleGoogleCheckResult(isSuccess, result);
        }
    }


    private void handleFbCheckResult(boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            int code = result.getCode();
            if (code > 0) {
                if (code == BasicResponseCodes.Oks.OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));

                            mLogger.debug("Facebook login OK");

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.FACEBOOK);
                            mAppConfiguration.getAppPrefs().save();

                            switchToState(State.FB_CHECK_OK);
                        } else {
                            switchToState(State.FB_CHECK_FAIL);
                            mLogger.error("Missing session info");
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Facebook login FAIL. JSON error:", result.getPayload());
                        switchToState(State.FB_CHECK_FAIL);
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    switchToState(State.FB_CHECK_FAIL);
                }
            } else {
                mLogger.debug("Facebook login FAIL. Code: {}", code);
                switchToState(State.FB_CHECK_FAIL);
            }
        } else {
            mLogger.debug("Facebook login FAIL");
            switchToState(State.FB_CHECK_FAIL);
        }
    }


    @Override
    public void checkGoogleLogin(String token) {
        mLogger.debug("Got google token", token);


        if (getState() == State.IDLE) {
            switchToState(State.WAITING_GOOGLE_CHECK);
            ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("login_google.php");

            b.addPostParameter("token", token);
            b.addPostParameter("app_type", "1");
            b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
            b.addPostParameter("session_info", "1");

            if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                b.addPostParameter("username", mAppConfiguration.getLoginPrefs().getUsername());
                b.addPostParameter("password", mAppConfiguration.getLoginPrefs().getPassword());
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
                if (code == BasicResponseCodes.Oks.OK.getCode()) {
                    try {
                        JSONObject jobj = new JSONObject(result.getPayload());
                        JSONObject sessionInfo = jobj.optJSONObject("session_info");
                        if (sessionInfo != null) {
                            int sessionTtl = jobj.getInt("session_ttl");
                            getSession().startSession(sessionTtl, Session.Info.fromJson(sessionInfo));


                            mLogger.debug("Google login OK");

                            mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.GOOGLE);
                            mAppConfiguration.getAppPrefs().save();

                            switchToState(State.GOOGLE_CHECK_OK);
                        } else {
                            switchToState(State.GOOGLE_CHECK_FAIL);
                            mLogger.error("Missing session info");
                        }
                    } catch (JSONException e) {
                        mLogger.debug("Google login FAIL. JSON error:", result.getPayload());
                        switchToState(State.GOOGLE_CHECK_FAIL);
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    switchToState(State.GOOGLE_CHECK_FAIL);
                }
            } else {
                mLogger.debug("Facebook login FAIL. Code: {}", code);
                switchToState(State.GOOGLE_CHECK_FAIL);
            }
        } else {
            mLogger.debug("Facebook login FAIL");
            switchToState(State.GOOGLE_CHECK_FAIL);
        }
    }


    @Override
    public void stateHandled() {
        if (isInOneOf(State.FB_CHECK_OK, State.FB_CHECK_FAIL, State.GOOGLE_CHECK_OK, State.GOOGLE_CHECK_FAIL)) {
            resetState();
        }
    }
}
