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


public class FacebookLoginHelperImpl implements FacebookLoginHelper {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ForgeExchangeManager mForgeExchangeManager;
    private final Session mSession;
    private final AppConfiguration mAppConfiguration;
    private final CurrentUserHolder mCurrentUserHolder;

    private Listener mListener;
    private volatile long mFacebookCheckXId;

    private volatile boolean mAbortLogin = false;



    @Inject
    public FacebookLoginHelperImpl(ForgeExchangeManager forgeExchangeManager,
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
        mAbortLogin = true;
    }


    @Override
    public void checkFbLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token) {
        mAbortLogin = false;
        mListener = listener;
        exchangeBuilder.addPostParameter("token", token);
        exchangeBuilder.addPostParameter("app_type", "1");
        exchangeBuilder.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        exchangeBuilder.addPostParameter("session_info", "1");

        mFacebookCheckXId = mForgeExchangeManager.generateTaskId();
        mForgeExchangeManager.executeExchange(exchangeBuilder.build(), mFacebookCheckXId);
    }


    @Override
    public boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mFacebookCheckXId == exchangeId) {
            if (mAbortLogin) {
                return true;
            }


            mFacebookCheckXId = 0;

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


                                mLogger.debug("Facebook login OK");

                                mAppConfiguration.getAppPrefs().setLastSuccessfulLoginMethod(LoginMethod.FACEBOOK);
                                mAppConfiguration.getAppPrefs().save();

                                mListener.onFacebookLoginOk();
                            } else {
                                mLogger.error("Missing session info");
                                mListener.onFacebookLoginFail(BasicResponseCodes.Errors.UNSPECIFIED_ERROR);
                            }
                        } catch (JSONException e) {
                            mLogger.debug("Facebook login FAIL. JSON error:", result.getPayload());
                            mListener.onFacebookLoginFail(code);
                        }
                    } else {
                        mLogger.debug("Facebook login FAIL. Code: {}", code);
                        mListener.onFacebookLoginFail(code);
                    }
                } else {
                    mLogger.debug("Facebook login FAIL. Code: {}", code);
                    mListener.onFacebookLoginFail(code);
                }
            } else {
                mLogger.debug("Facebook login FAIL");
                mListener.onFacebookLoginFail(BasicResponseCodes.Errors.UNSPECIFIED_ERROR);
            }
            return true;
        } else {
            return false;
        }
    }
}
