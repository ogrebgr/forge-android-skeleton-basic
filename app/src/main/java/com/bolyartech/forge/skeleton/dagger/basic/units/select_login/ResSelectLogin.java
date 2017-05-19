package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;


import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface ResSelectLogin extends MultiOperationResidentComponent<ResSelectLogin.Operation> {

    void checkFbLogin(String token);

    void checkGoogleLogin(String token);

    enum Operation {
        FACEBOOK_LOGIN,
        GOOGLE_LOGIN
    }
}


