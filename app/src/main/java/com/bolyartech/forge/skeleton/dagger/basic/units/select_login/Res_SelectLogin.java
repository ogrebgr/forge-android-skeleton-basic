package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.google.android.gms.common.api.GoogleApiClient;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface Res_SelectLogin {
    enum State {
        IDLE,
        WAITING_FB_CHECK,
        FB_CHECK_OK,
        FB_CHECK_FAIL,
        WAITING_GOOGLE_CHECK,
        GOOGLE_CHECK_OK,
        GOOGLE_CHECK_FAIL
    }

    State getState();

    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(GoogleApiClient googleApiClient);

    void logout();

    void resetState();
}
