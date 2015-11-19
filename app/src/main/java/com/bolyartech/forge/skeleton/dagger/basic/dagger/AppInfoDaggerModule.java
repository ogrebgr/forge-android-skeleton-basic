package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class AppInfoDaggerModule {
    private final String mAppKey;
    private final String mAppVersion;


    public AppInfoDaggerModule(String appKey, String appVersion) {
        mAppKey = appKey;
        mAppVersion = appVersion;
    }


    @Provides
    @Named("app key")
    String provideAppKey() {
        return mAppKey;
    }


    @Provides
    @Named("app version")
    String provideAppVersion() {
        return mAppVersion;
    }
}
