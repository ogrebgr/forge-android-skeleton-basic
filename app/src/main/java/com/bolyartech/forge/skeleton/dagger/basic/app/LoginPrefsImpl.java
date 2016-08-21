package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.ForApplication;

import javax.inject.Inject;


public class LoginPrefsImpl implements LoginPrefs {
    public static final String KEY_USERNAME = "Username";
    public static final String KEY_PASSWORD = "Password";
    public static final String KEY_MANUAL_REG = "Manual registration";
    public static final String KEY_PUBLIC_NAME = "Public name";

    private static final String PREFERENCES_FILE = "Login prefs";
    private final SharedPreferences mPrefs;

    private boolean mNeedSave = false;

    private String mUsername;
    private String mPassword;
    private String mPublicName;

    // Indicates that user registered with username and password (in contrast to auto registration with random generated)
    private boolean mManualReg;


    @Inject
    public LoginPrefsImpl(@ForApplication Context ctx) {
        mPrefs = ctx.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

        mUsername = mPrefs.getString(KEY_USERNAME, null);
        mPassword = mPrefs.getString(KEY_PASSWORD, null);
        mManualReg = mPrefs.getBoolean(KEY_MANUAL_REG, false);
        mPublicName = mPrefs.getString(KEY_USERNAME, null);
    }


    @Override
    public String getUsername() {
        return mUsername;
    }


    @Override
    public void setUsername(String username) {
        if (mUsername == null || (username != null && !mUsername.equals(username))) {
            mUsername = username;
            mNeedSave = true;
        }
    }


    @Override
    public String getPassword() {
        return mPassword;
    }


    @Override
    public void setPassword(String password) {
        if (mPassword == null || (password != null && !mPassword.equals(password))) {
            mPassword = password;
            mNeedSave = true;
        }
    }


    @Override
    public void save() {
        if (mNeedSave) {
            Editor ed = mPrefs.edit();
            ed.putString(KEY_USERNAME, mUsername);
            ed.putString(KEY_PASSWORD, mPassword);
            ed.putBoolean(KEY_MANUAL_REG, mManualReg);
            ed.putString(KEY_PUBLIC_NAME, mPublicName);
            ed.apply();
        }
    }


    public boolean hasLoginCredentials() {
        return StringUtils.isNotEmpty(mUsername) && StringUtils.isNotEmpty(mPassword);
    }


    @Override
    public boolean isManualRegistration() {
        return mManualReg;
    }


    @Override
    public void setManualRegistration(boolean val) {
        if (mManualReg != val) {
            mManualReg = val;
            mNeedSave = true;
        }
    }


    @Override
    public String getPublicName() {
        return mPublicName;
    }


    @Override
    public void setPublicName(String publicName) {
        if (mPublicName == null || (publicName != null && !mPublicName.equals(publicName))) {
            mPublicName = publicName;
            mNeedSave = true;
        }

    }
}
