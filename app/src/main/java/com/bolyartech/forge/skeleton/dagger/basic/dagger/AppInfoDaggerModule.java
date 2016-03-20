package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfigurationImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.ForgeExchangeHelper;
import com.bolyartech.forge.skeleton.dagger.basic.app.ForgeExchangeHelperImpl;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class AppInfoDaggerModule {
    private final String mAppVersion;


    public AppInfoDaggerModule(String appKey, String appVersion) {
        mAppVersion = appVersion;
    }


    @Provides
    @Named("app version")
    String provideAppVersion() {
        return mAppVersion;
    }


    @Provides
    @Singleton
    AppConfiguration provideAppConfiguration(AppConfigurationImpl impl) {
        return impl;
    }

    @Provides
    @Singleton
    ForgeExchangeHelper provideForgeExchangeHelper(ForgeExchangeHelperImpl impl) {
        return impl;
    }
}
