package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.skeleton.dagger.basic.misc.AppLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.AppLoginHelperImpl;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelperImpl;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GoogleLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GoogleLoginHelperImpl;
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


    //TODO remove
    @Provides
    AppLoginHelper provideLoginHelper(AppLoginHelperImpl impl) {
        return impl;
    }


    @Provides
    FacebookLoginHelper provideFacebookLoginHelper(FacebookLoginHelperImpl impl) {
        return impl;
    }


    @Provides
    GoogleLoginHelper provideGoogleLoginHelper(GoogleLoginHelperImpl impl) {
        return impl;
    }


    @Provides
    LoginTask provideLoginTask(LoginTaskImpl impl) {
        return impl;
    }
}
