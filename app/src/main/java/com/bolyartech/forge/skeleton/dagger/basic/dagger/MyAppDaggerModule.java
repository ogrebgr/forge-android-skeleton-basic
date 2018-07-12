package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import android.content.Context;

import com.bolyartech.forge.android.misc.AndroidTimeProvider;
import com.bolyartech.forge.base.misc.TimeProvider;
import com.bolyartech.forge.skeleton.dagger.basic.app.App;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefsImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefsImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-11-15 15:20
 */
@Module
public class MyAppDaggerModule {
    private final App mApp;


    public MyAppDaggerModule(App app) {
        mApp = app;
    }

    @Provides
    @ForApplication
    Context providesApplicationContext() {
        return mApp;
    }


    @Provides
    @Singleton
    AppPrefs provideAppPrefs() {
        return new AppPrefsImpl(mApp);
    }


    @Provides
    @Singleton
    LoginPrefs provideLoginPrefs(LoginPrefsImpl impl) {
        return impl;
    }


    @Provides
    TimeProvider providesTimeProvider() {
        return new AndroidTimeProvider();
    }


    @Provides
    @Singleton
    CurrentUserHolder provideCurrentUserHolder() {
        return new CurrentUserHolder();
    }


}
