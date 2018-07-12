package com.bolyartech.forge.skeleton.dagger.basic.app;

/**
 * Created by ogre on 2015-07-16
 */

import android.content.Context;
import android.content.SharedPreferences;

import com.bolyartech.forge.skeleton.dagger.basic.dagger.ForApplication;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import javax.inject.Inject;


public class AppPrefsImpl implements AppPrefs {
    private static final String PREFERENCES_FILE = "forge";

    private static final String KEY_GCM_TOKEN = "gcm token";
    private static final String KEY_GCM_TOKEN_SENT = "gcm token sent";

    private static final String KEY_USER_ID = "user_id";

    private static final String KEY_LAST_SUCCESSFUL_LOGIN_METHOD = "last successful login method";
    private static final String KEY_SELECTED_LOGIN_METHOD = "selected login method";

    private final SharedPreferences mPrefs;

    private String mGcmToken;
    private boolean mGcmTokenSent;
    private LoginMethod mLastSuccessfulLoginMethod;
    private LoginMethod mSelectedLoginMethod;
    private boolean mAskedToShareOnFacebook;


    @Inject
    public AppPrefsImpl(@ForApplication Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

        mGcmToken = mPrefs.getString(KEY_GCM_TOKEN, null);
        mGcmTokenSent = mPrefs.getBoolean(KEY_GCM_TOKEN_SENT, false);
        mLastSuccessfulLoginMethod = LoginMethod.fromInt(mPrefs.getInt(KEY_LAST_SUCCESSFUL_LOGIN_METHOD, 0));
        mSelectedLoginMethod = LoginMethod.fromInt(mPrefs.getInt(KEY_SELECTED_LOGIN_METHOD, -1));
    }


    @Override
    public String getGcmToken() {
        return mGcmToken;
    }


    @Override
    public void setGcmToken(String token) {
        mGcmToken = token;
    }


    @Override
    public boolean isGcmTokenUpdatedOnServer() {
        return mGcmTokenSent;
    }


    @Override
    public void setGcmTokenUpdatedOnServer(boolean sent) {
        mGcmTokenSent = sent;
    }


    @Override
    public void save() {
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putString(KEY_GCM_TOKEN, mGcmToken);
        ed.putBoolean(KEY_GCM_TOKEN_SENT, mGcmTokenSent);
        ed.putInt(KEY_LAST_SUCCESSFUL_LOGIN_METHOD, mLastSuccessfulLoginMethod.getCode());
        if (mSelectedLoginMethod != null) {
            ed.putInt(KEY_SELECTED_LOGIN_METHOD, mSelectedLoginMethod.getCode());
        } else {
            ed.putInt(KEY_SELECTED_LOGIN_METHOD, -1);
        }

        ed.apply();
    }


    @Override
    public boolean getAskedToShareOnFacebook() {
        return mAskedToShareOnFacebook;
    }


    @Override
    public void setAskedToShareOnFacebook(boolean asked) {
        mAskedToShareOnFacebook = asked;
    }


    @Override
    public LoginMethod getLastSuccessfulLoginMethod() {
        return mLastSuccessfulLoginMethod;
    }


    @Override
    public void setLastSuccessfulLoginMethod(LoginMethod method) {
        mLastSuccessfulLoginMethod = method;
    }


    @Override
    public LoginMethod getSelectedLoginMethod() {
        return mSelectedLoginMethod;
    }


    @Override
    public void setSelectedLoginMethod(LoginMethod method) {
        mSelectedLoginMethod = method;
    }
}
