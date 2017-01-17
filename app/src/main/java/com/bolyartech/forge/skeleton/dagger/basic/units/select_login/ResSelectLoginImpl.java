package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GoogleLoginHelper;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class ResSelectLoginImpl extends AbstractMultiOperationResidentComponent<ResSelectLogin.Operation>
        implements ResSelectLogin, FacebookLoginHelper.Listener, GoogleLoginHelper.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final ForgeExchangeHelper mForgeExchangeHelper;

    private final FacebookLoginHelper mFacebookLoginHelper;
    private final GoogleLoginHelper mGoogleLoginHelper;



    @Inject
    public ResSelectLoginImpl(FacebookLoginHelper facebookLoginHelper,
                              ForgeExchangeHelper forgeExchangeHelper,
                              GoogleLoginHelper googleLoginHelper) {

        mFacebookLoginHelper = facebookLoginHelper;
        mGoogleLoginHelper = googleLoginHelper;
        mForgeExchangeHelper = forgeExchangeHelper;
    }


    @Override
    public void checkFbLogin(String token) {
        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState(Operation.FACEBOOK_LOGIN);
            mFacebookLoginHelper.checkFbLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_facebook"), this, token);
        }
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (!mGoogleLoginHelper.handleExchange(exchangeId, isSuccess, result)) {
            mFacebookLoginHelper.handleExchange(exchangeId, isSuccess, result);
        }
    }


    @Override
    public void onFacebookLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onFacebookLoginFail(int code) {
        switchToEndedStateFail();
    }


    @Override
    public void checkGoogleLogin(String token) {
        mLogger.debug("Got google token", token);

        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState(Operation.GOOGLE_LOGIN);
            mGoogleLoginHelper.checkGoogleLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_google"), this, token);
        } else {
            mLogger.warn("checkGoogleLogin(): Not in state IDLE");
        }
    }


    @Override
    public void onGoogleLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onGoogleLoginFail(int code) {
        switchToEndedStateFail();
    }
}
