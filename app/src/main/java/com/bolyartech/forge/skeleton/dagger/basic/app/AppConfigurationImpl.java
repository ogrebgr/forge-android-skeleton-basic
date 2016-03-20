package com.bolyartech.forge.skeleton.dagger.basic.app;


import android.content.Context;

import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForApplication;

import javax.inject.Inject;
import javax.inject.Named;


public class AppConfigurationImpl implements AppConfiguration {
    private final String mAppVersion;

    private final AppPrefs mAppPrefs;

    private final LoginPrefs mLoginPrefs;

    private final boolean mShallAutoregister;

    private final boolean mShallUseGsm;


    @Inject
    public AppConfigurationImpl(@Named("app version") String appVersion,
                                AppPrefs appPrefs,
                                LoginPrefs loginPrefs,
                                @ForApplication Context appContext
                                ) {
        mAppVersion = appVersion;
        mAppPrefs = appPrefs;
        mLoginPrefs = loginPrefs;

        mShallAutoregister = appContext.getResources().getBoolean(R.bool.app_conf__do_autoregister);
        mShallUseGsm = appContext.getResources().getBoolean(R.bool.app_conf__use_gcm);
    }


    @Override
    public String getAppVersion() {
        return mAppVersion;
    }


    @Override
    public AppPrefs getAppPrefs() {
        return mAppPrefs;
    }


    @Override
    public LoginPrefs getLoginPrefs() {
        return mLoginPrefs;
    }


    @Override
    public boolean shallAutoregister() {
        return mShallAutoregister;
    }


    @Override
    public boolean shallUseGcm() {
        return mShallUseGsm;
    }
}
