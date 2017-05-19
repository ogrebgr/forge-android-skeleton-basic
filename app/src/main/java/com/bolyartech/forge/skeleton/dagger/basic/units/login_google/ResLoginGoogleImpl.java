package com.bolyartech.forge.skeleton.dagger.basic.units.login_google;

import com.bolyartech.forge.android.app_unit.OpState;
import com.bolyartech.forge.android.app_unit.OperationResidentComponentImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
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
    public void onGoogleLoginOk() {
        switchToEndedStateSuccess();
    }


    @Override
    public void onGoogleLoginFail(int code) {
        switchToEndedStateFail();
    }


    @Override
    public void checkGoogleLogin(String token) {
        if (getOpState() == OpState.IDLE) {
            switchToBusyState();
            mGoogleLoginHelper.checkGoogleLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_google"), this, token);
        }
    }
}
