package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.AbstractSideEffectOperationResidentComponent;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * Created by ogre on 2016-01-01 14:37
 */
public class ResRegisterImpl extends AbstractSideEffectOperationResidentComponent<Void, Integer> implements ResRegister {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());


    private volatile long mRegisterXId;
    private volatile long mPostAutoRegisterXId;

    private String mLastUsedUsername;
    private String mLastUsedPassword;

    private final AppConfiguration mAppConfiguration;

    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;

    @Inject
    CurrentUserHolder mCurrentUserHolder;

    @Inject
    public ResRegisterImpl(AppConfiguration appConfiguration,
                           ForgeExchangeHelper forgeExchangeHelper, Session session) {

        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;

        mAppConfiguration = appConfiguration;
    }


    @Override
    public void register(String username, String password, String screenName) {
        if (getOpState() == OpState.IDLE) {
            switchToBusyState();

            mLastUsedUsername = username;
            mLastUsedPassword = password;

            if (StringUtils.isEmpty(mAppConfiguration.getLoginPrefs().getUsername())) {
                normalRegistration(username, password, screenName);
            } else {
                if (!mAppConfiguration.getLoginPrefs().isManualRegistration()) {
                    postAutoRegistration(username, password, screenName);
                } else {
                    // register() method should not been called in this condition
                    switchToCompletedStateFail(null);
                }
            }
        } else {
            mLogger.error("register() called not in IDLE state. Ignoring.");
        }
    }


    private void postAutoRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("register_postauto");

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

        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        mPostAutoRegisterXId = em.generateTaskId();
        mLogger.debug("mPostAutoRegisterXId {}", mPostAutoRegisterXId);
        em.executeExchange(b.build(), mPostAutoRegisterXId);
    }


    private void normalRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("register");

        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("screen_name", screenName);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");
        b.addPostParameter("do_login", "1");

        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        mRegisterXId = em.generateTaskId();
        em.executeExchange(b.build(), mRegisterXId);
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mRegisterXId == exchangeId) {
            if (handleRegistrationCommon1(isSuccess, result)) {
                try {
                    JSONObject jobj = new JSONObject(result.getPayload());
                    int sessionTtl = jobj.getInt("session_ttl");

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        mSession.startSession(sessionTtl);

                        mCurrentUserHolder.setCurrentUser(new CurrentUser(sessionInfo.getLong("user_id"),
                                sessionInfo.getString("screen_name")));

                        handleRegistrationCommon2();
                    } else {
                        switchToCompletedStateFail(null);
                    }
                } catch (JSONException e) {
                    mLogger.warn("Register exchange failed because cannot parse JSON");
                    switchToCompletedStateFail(null);
                }
            }
        } else if (mPostAutoRegisterXId == exchangeId) {
            if (handleRegistrationCommon1(isSuccess, result)) {
                handleRegistrationCommon2();
            }
        }
    }


    private boolean handleRegistrationCommon1(boolean isSuccess, ForgeExchangeResult result) {
        if (!isSuccess) {
            mLogger.warn("Register exchange failed");
            switchToCompletedStateFail(null);
            return false;
        }

        int code = result.getCode();

        if (code != BasicResponseCodes.OK) {
            mLogger.warn("Register exchange failed with code {}", code);
            switchToCompletedStateFail(code);

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
        switchToCompletedStateSuccess(null);
    }
}


