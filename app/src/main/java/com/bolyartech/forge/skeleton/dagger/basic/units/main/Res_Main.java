package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;

import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;


/**
 * Created by ogre on 2015-10-05
 */
public interface Res_Main {
    State getState();

    void login();

    void startSession();

    boolean isFacebookManualReloginNeeded();

    void clearFacebookManualReloginNeeded();

    void onFacebookActivityResult(int requestCode, int resultCode, Intent data);

    void abortLogin();

    void logout();

    void internetAvailable();

    LoginMethod getLastAttemptedLoginMethod();

    boolean isGoogleNativeLoginFail();

    boolean isFacebookNativeLoginFail();

    void resetState();

    void onConnectivityChange();

    enum State {
        NO_INET,
        IDLE,
        AUTO_REGISTERING,
        LOGGING_IN,
        STARTING_SESSION,
        SESSION_STARTED_OK,
        SESSION_START_FAIL,
        LOGIN_INVALID,
        LOGIN_FAIL,
        UPGRADE_NEEDED,
        REGISTER_AUTO_FAIL
    }
}
