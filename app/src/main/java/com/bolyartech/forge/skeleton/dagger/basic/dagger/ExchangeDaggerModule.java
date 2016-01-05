package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.exchange.ExchangeFunctionality;
import com.bolyartech.forge.exchange.ExchangeFunctionalityImpl;
import com.bolyartech.forge.exchange.ExchangeManager;
import com.bolyartech.forge.exchange.ForgeExchangeFunctionality;
import com.bolyartech.forge.exchange.ForgeExchangeManager;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.http.functionality.HttpFunctionalityWCookies;
import com.bolyartech.forge.misc.ForgeExchangeManagerImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppExchangeManager;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module(includes = {HttpsDaggerModule.class})
public class ExchangeDaggerModule {
    private final String mBaseUrl;


    public ExchangeDaggerModule(String baseUrl) {
        super();
        mBaseUrl = baseUrl;
    }


    @Provides
    @Named("base url")
    String provideBaseUrl() {
        return mBaseUrl;
    }



    @Provides
    @Singleton
    ForgeExchangeFunctionality provide(HttpFunctionalityWCookies httpFunc) {
        return new ForgeExchangeFunctionality(httpFunc);
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager(MyAppExchangeManager impl) {
        return impl;
    }
}
