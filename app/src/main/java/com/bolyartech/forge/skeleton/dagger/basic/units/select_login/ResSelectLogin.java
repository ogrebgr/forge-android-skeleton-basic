package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;


import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface ResSelectLogin extends MultiOperationResidentComponent<ResSelectLogin.Operation>, ForgeExchangeManagerListener {

    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

    enum Operation {
        FACEBOOK_LOGIN,
        GOOGLE_LOGIN
    }
}


