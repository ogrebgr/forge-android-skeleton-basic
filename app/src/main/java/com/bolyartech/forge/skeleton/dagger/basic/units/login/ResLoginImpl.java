package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.AbstractSideEffectOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.AppLoginHelper;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2016-01-05 14:26
 */
public class ResLoginImpl extends AbstractSideEffectOperationResidentComponent<Void, Integer> implements ResLogin,
        AppLoginHelper.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final ForgeExchangeHelper mForgeExchangeHelper;
    Provider<AppLoginHelper> mLoginHelperProvider;
    private AppLoginHelper mAppLoginHelper;


    @Inject
    public ResLoginImpl(ForgeExchangeHelper forgeExchangeHelper, Provider<AppLoginHelper> loginHelperProvider) {
        mForgeExchangeHelper = forgeExchangeHelper;
        mLoginHelperProvider = loginHelperProvider;
    }


    @Override
    public void login(String username, String password) {
        if (isIdle()) {
            switchToBusyState();

            mAppLoginHelper = mLoginHelperProvider.get();
            mAppLoginHelper.initiate(mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                    mForgeExchangeHelper.createForgePostHttpExchangeBuilder("login"),
                    username,
                    password,
                    this,
                    false);
        }
    }


    @Override
    public void abortLogin() {
        if (mAppLoginHelper != null) {
            mAppLoginHelper.abortLogin();
        }
        abort();
    }


    @Override
    public void onLoginOk() {
        switchToEndedStateSuccess(null);
    }


    @Override
    public void onLoginFail(int code) {
        switchToEndedStateFail(code);
    }
}
