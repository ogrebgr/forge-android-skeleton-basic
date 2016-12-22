package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginHelperImpl;
import com.bolyartech.scram_sasl.client.ScramClientFunctionality;
import com.bolyartech.scram_sasl.client.ScramClientFunctionalityImpl;


import dagger.Module;
import dagger.Provides;


@Module
public class LoginModule {
    private static final String DIGEST = "SHA-512";
    private static final String HMAC = "HmacSHA512";


    @Provides
    ScramClientFunctionality provideScramClientFunctionality() {
        return new ScramClientFunctionalityImpl(DIGEST, HMAC);
    }

    @Provides
    LoginHelper provideLoginHelper(LoginHelperImpl impl) {
        return impl;
    }

}
