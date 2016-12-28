package com.bolyartech.forge.skeleton.dagger.basic.utils;

import android.content.Context;

import com.bolyartech.forge.android.misc.AndroidTimeProvider;
import com.bolyartech.forge.base.misc.TimeProvider;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.App;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.ForApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module
public class FakeMyAppDaggerModule {
    private final App mApp;
    private final FakeAppPrefs mAppPrefs;
    private final FakeLoginPrefs mFakeLoginPrefs;
    private final CurrentUserHolder mCurrentUserHolder;


    public FakeMyAppDaggerModule(App app,
                                 FakeAppPrefs appPrefs,
                                 FakeLoginPrefs fakeLoginPrefs,
                                 CurrentUserHolder currentUserHolder) {
        mApp = app;
        mAppPrefs = appPrefs;
        mFakeLoginPrefs = fakeLoginPrefs;
        mCurrentUserHolder = currentUserHolder;
    }


    @Provides
    @ForApplication
    Context providesApplicationContext() {
        return mApp;
    }


    @Provides
    @Singleton
    AppPrefs provideAppPrefs() {
        return mAppPrefs;
    }


    @Provides
    @Singleton
    LoginPrefs provideLoginPrefs() {
        return mFakeLoginPrefs;
    }


    @Provides
    TimeProvider providesTimeProvider() {
        return new AndroidTimeProvider();
    }


    @Provides
    @Singleton
    CurrentUserHolder provideCurrentUserHolder() {
        return mCurrentUserHolder;
    }

}
