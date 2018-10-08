package com.bolyartech.forge.skeleton.dagger.basic.app;

/**
 * Created by ogre on 2015-07-16
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.bolyartech.forge.skeleton.dagger.basic.dagger.ForApplication;

import javax.inject.Inject;


public class AppPrefsImpl implements AppPrefs {
    private static final String PREFERENCES_FILE = "forge";

    private static final String KEY_USER_ID = "user_id";

    private static final String KEY_LAST_SUCCESSFUL_LOGIN_METHOD = "last successful login method";
    private static final String KEY_SELECTED_LOGIN_METHOD = "selected login method";

    private final SharedPreferences mPrefs;

    private boolean mAskedToShareOnFacebook;


    @Inject
    public AppPrefsImpl(@ForApplication Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }


    @Override
    public void save() {
        SharedPreferences.Editor ed = mPrefs.edit();

        ed.apply();
    }
}
