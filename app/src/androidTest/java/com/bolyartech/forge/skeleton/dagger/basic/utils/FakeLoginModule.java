package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.misc.AppLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelper;
import com.bolyartech.forge.skeleton.dagger.basic.misc.FacebookLoginHelperImpl;
import com.bolyartech.scram_sasl.client.ScramClientFunctionality;
import com.bolyartech.scram_sasl.client.ScramClientFunctionalityImpl;

import dagger.Module;
import dagger.Provides;


@Module
public class FakeLoginModule {
    private static final String DIGEST = "SHA-512";
    private static final String HMAC = "HmacSHA512";


    @Provides
    ScramClientFunctionality provideScramClientFunctionality() {
        return new ScramClientFunctionalityImpl(DIGEST, HMAC);
    }


    @Provides
    AppLoginHelper provideLoginHelper(ForgeExchangeManager forgeExchangeManager,
                                      ScramClientFunctionality scramClientFunctionality,
                                      Session session,
                                      AppConfiguration appConfiguration,
                                      CurrentUserHolder currentUserHolder) {
        return new AppLoginHelper() {


            @Override
            public void initiate(ForgePostHttpExchangeBuilder step1builder, ForgePostHttpExchangeBuilder step2builder, String username, String password, Listener listener, boolean autologin) {

            }


            @Override
            public void abortLogin() {

            }


            @Override
            public boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
                return false;
            }
        };
    }


    @Provides
    FacebookLoginHelper provideFacebookLoginHelper() {
        return new FacebookLoginHelper() {

            @Override
            public void abortLogin() {

            }


            @Override
            public void checkFbLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token) {

            }


            @Override
            public boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
                return false;
            }
        };
    }
}
