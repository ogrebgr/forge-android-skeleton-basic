package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import android.content.Context;

import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefsImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefsImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyApp;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-11-15 15:20
 */
@Module
public class MyAppDaggerModule {
    private final MyApp mMyApp;


    public MyAppDaggerModule(MyApp myApp) {
        mMyApp = myApp;
    }

    @Provides
    @ForApplication
    Context providesApplicationContext() {
        return mMyApp;
    }


    @Provides
    @Singleton
    AppPrefs provideAppPrefs() {
        return new AppPrefsImpl(mMyApp);
    }


    @Provides
    @Singleton
    LoginPrefs provideLoginPrefs(LoginPrefsImpl impl) {
        return impl;
    }



}
