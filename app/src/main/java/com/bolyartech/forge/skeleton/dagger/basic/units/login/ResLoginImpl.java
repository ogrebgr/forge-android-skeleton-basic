package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.AbstractSideEffectOperationResidentComponent;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
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
 * Created by ogre on 2016-01-05 14:26
 */
public class ResLoginImpl extends AbstractSideEffectOperationResidentComponent<Void, Integer> implements ResLogin {
    private volatile long mLoginXId;
    private volatile boolean mAbortLogin = false;

    private String mLastUsedUsername;
    private String mLastUsedPassword;


    private final AppConfiguration mAppConfiguration;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;


    @Inject
    CurrentUserHolder mCurrentUserHolder;

    @Inject
    public ResLoginImpl(
            AppConfiguration appConfiguration,
            ForgeExchangeHelper forgeExchangeHelper,
            Session session) {

        mAppConfiguration = appConfiguration;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
    }


    @Override
    public void login(String username, String password) {
        switchToBusyState();
        mLastUsedUsername = username;
        mLastUsedPassword = password;

        ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login");
        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());

        ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
        mLoginXId = em.generateTaskId();
        em.executeExchange(b.build(), mLoginXId);
    }


    @Override
    public void abortLogin() {
        mAbortLogin = true;
        abort();
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (exchangeId == mLoginXId) {
            if (!mAbortLogin) {
                if (isSuccess) {
                    int code = result.getCode();

                    if (code > 0) {
                        if (code == BasicResponseCodes.OK) {
                            try {
                                JSONObject jobj = new JSONObject(result.getPayload());
                                int sessionTtl = jobj.getInt("session_ttl");
                                JSONObject sessionInfo = jobj.optJSONObject("session_info");
                                if (sessionInfo != null) {
                                    mSession.startSession(sessionTtl);

                                    mCurrentUserHolder.setCurrentUser(new CurrentUser(sessionInfo.getLong("user_id"),
                                            sessionInfo.optString("screen_name_chosen", null),
                                            sessionInfo.optString("screen_name_default", null)));

                                    mLogger.debug("App login OK");

                                    mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.APP);
                                    mAppConfiguration.getAppPrefs().save();

                                    LoginPrefs lp = mAppConfiguration.getLoginPrefs();
                                    lp.setUsername(mLastUsedUsername);
                                    lp.setPassword(mLastUsedPassword);
                                    lp.setManualRegistration(true);
                                    lp.save();

                                    switchToEndedStateSuccess(null);
                                } else {
                                    mLogger.error("Missing session info");
                                    switchToEndedStateFail(null);

                                }
                            } catch (JSONException e) {
                                mLogger.warn("Login exchange failed because cannot parse JSON");
                                switchToEndedStateFail(null);
                            }
                        } else {
                            // unexpected positive code
                            switchToEndedStateFail(null);
                        }
                    } else {

                        mLogger.warn("Login exchange failed with code {}", code);
                        switchToEndedStateFail(code);
                    }
                } else {
                    switchToEndedStateFail(null);
                }
            }
        }
    }
}
