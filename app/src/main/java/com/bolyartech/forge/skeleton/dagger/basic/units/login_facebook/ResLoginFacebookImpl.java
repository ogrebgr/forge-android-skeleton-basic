package com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook;

import com.bolyartech.forge.android.app_unit.OpState;
import com.bolyartech.forge.android.app_unit.OperationResidentComponentImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;

import javax.inject.Inject;


public class ResLoginFacebookImpl extends OperationResidentComponentImpl implements ResLoginFacebook,
        FacebookLoginHelper.Listener {

    private final ForgeExchangeHelper mForgeExchangeHelper;

    private final FacebookLoginHelper mFacebookLoginHelper;


    @Inject
    public ResLoginFacebookImpl(ForgeExchangeHelper forgeExchangeHelper,
                                FacebookLoginHelper facebookLoginHelper) {

        mForgeExchangeHelper = forgeExchangeHelper;
        mFacebookLoginHelper = facebookLoginHelper;
    }


    @Override
    public void checkFbLogin(String token) {
        if (getOpState() == OpState.IDLE) {
            switchToBusyState();
            mFacebookLoginHelper.checkFbLogin(mForgeExchangeHelper.
                    createForgePostHttpExchangeBuilder("login_facebook"), this, token);
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
}