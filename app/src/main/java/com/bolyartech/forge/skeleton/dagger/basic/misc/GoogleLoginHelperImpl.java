package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class GoogleLoginHelperImpl implements GoogleLoginHelper {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ForgeExchangeManager mForgeExchangeManager;
    private final Session mSession;
    private final AppConfiguration mAppConfiguration;
    private final CurrentUserHolder mCurrentUserHolder;

    private Listener mListener;
    private volatile long mGoogleCheckXId;

    private volatile boolean mAbortLogin = false;


    @Inject
    public GoogleLoginHelperImpl(ForgeExchangeManager forgeExchangeManager,
                                 Session session,
                                 AppConfiguration appConfiguration,
                                 CurrentUserHolder currentUserHolder) {

        mForgeExchangeManager = forgeExchangeManager;
        mSession = session;
        mAppConfiguration = appConfiguration;
        mCurrentUserHolder = currentUserHolder;
    }


    @Override
    public void abortLogin() {

    }


    @Override
    public void checkGoogleLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token) {
        mAbortLogin = false;
        mListener = listener;

        exchangeBuilder.addPostParameter("token", token);
        exchangeBuilder.addPostParameter("app_type", "1");
        exchangeBuilder.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        exchangeBuilder.addPostParameter("session_info", "1");

        mGoogleCheckXId = mForgeExchangeManager.generateTaskId();
        mForgeExchangeManager.executeExchange(exchangeBuilder.build(), mGoogleCheckXId);
    }


    @Override
    public boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mGoogleCheckXId == exchangeId) {
            if (mAbortLogin) {
                return true;
            }

            if (isSuccess) {
                int code = result.getCode();
                if (code > 0) {
                    if (code == BasicResponseCodes.OK) {
                        try {
                            JSONObject jobj = new JSONObject(result.getPayload());
                            JSONObject sessionInfo = jobj.optJSONObject("session_info");
                            if (sessionInfo != null) {
                                int sessionTtl = jobj.getInt("session_ttl");
                                mSession.startSession(sessionTtl);

                                mCurrentUserHolder.setCurrentUser(
                                        new CurrentUser(sessionInfo.getLong("user_id"),
                                                sessionInfo.optString("screen_name", null)));

                                mLogger.debug("Google login OK");

                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.GOOGLE);
                                mAppConfiguration.getAppPrefs().save();

                                mListener.onGoogleLoginOk();
                            } else {
                                mLogger.error("Missing session info");
                                mListener.onGoogleLoginFail(code);
                            }
                        } catch (JSONException e) {
                            mLogger.debug("Google login FAIL. JSON error:", result.getPayload());
                            mListener.onGoogleLoginFail(code);
                        }
                    } else {
                        mLogger.debug("Google login FAIL. Code: {}", code);
                        mListener.onGoogleLoginFail(code);
                    }
                } else {
                    mLogger.debug("Google login FAIL. Code: {}", code);
                    mListener.onGoogleLoginFail(code);
                }
            } else {
                mLogger.debug("Google login FAIL");
                mListener.onGoogleLoginFail(BasicResponseCodes.Errors.UNSPECIFIED_ERROR);
            }

            return true;
        } else {
            return false;
        }
    }
}
