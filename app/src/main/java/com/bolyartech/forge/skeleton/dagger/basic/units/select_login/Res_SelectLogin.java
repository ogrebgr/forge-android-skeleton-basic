package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;


import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface Res_SelectLogin extends StatefulResidentComponent<Res_SelectLogin.State> {

    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

    void logout();


    enum State {
        IDLE,
        WAITING_FB_CHECK,
        FB_CHECK_OK,
        FB_CHECK_FAIL,
        WAITING_GOOGLE_CHECK,
        GOOGLE_CHECK_OK,
        GOOGLE_CHECK_FAIL;
    }
}


