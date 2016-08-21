package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;


import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface Res_SelectLogin extends MultiOperationResidentComponent<Res_SelectLogin.Operation>, ForgeExchangeManagerListener {

    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

    LoginResult getLoginResult();

//    enum State {
//        IDLE,
//        WAITING_FB_CHECK,
//        FB_CHECK_OK,
//        FB_CHECK_FAIL,
//        WAITING_GOOGLE_CHECK,
//        GOOGLE_CHECK_OK,
//        GOOGLE_CHECK_FAIL;
//    }

    enum Operation {
        FACEBOOK_LOGIN,
        GOOGLE_LOGIN
    }


    enum LoginResult {
        SUCCESS,
        FAIL
    }
}


