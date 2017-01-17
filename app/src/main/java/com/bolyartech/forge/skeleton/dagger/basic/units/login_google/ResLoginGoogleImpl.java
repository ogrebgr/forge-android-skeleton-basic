package com.bolyartech.forge.skeleton.dagger.basic.units.login_google;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponentImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GoogleLoginHelper;

import javax.inject.Inject;


public class ResLoginGoogleImpl extends OperationResidentComponentImpl implements ResLoginGoogle,
        GoogleLoginHelper.Listener {


    private final ForgeExchangeHelper mForgeExchangeHelper;

    private final GoogleLoginHelper mGoogleLoginHelper;


    @Inject
    public ResLoginGoogleImpl(ForgeExchangeHelper forgeExchangeHelper, GoogleLoginHelper facebookLoginHelper) {
        mForgeExchangeHelper = forgeExchangeHelper;
        mGoogleLoginHelper = facebookLoginHelper;
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        mGoogleLoginHelper.handleExchange(exchangeId, isSuccess, result);
    }


    @Override
    public void onGoogleLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onGoogleLoginFail(int code) {
        switchToEndedStateFail();
    }


    @Override
    public void checkGoogleLogin(String token) {
        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState();
            mGoogleLoginHelper.checkGoogleLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_google"), this, token);
        }
    }
}
