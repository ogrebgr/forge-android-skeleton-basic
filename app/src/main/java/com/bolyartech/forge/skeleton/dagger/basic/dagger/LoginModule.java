package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.skeleton.dagger.basic.units.login.LoginTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.LoginTaskImpl;
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
    LoginTask provideLoginTask(LoginTaskImpl impl) {
        return impl;
    }
}
