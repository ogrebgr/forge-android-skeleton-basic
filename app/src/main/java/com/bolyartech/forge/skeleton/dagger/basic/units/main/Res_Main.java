package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;


/**
 * Created by ogre on 2015-10-05
 */
public interface Res_Main extends MultiOperationResidentComponent<Res_Main.Operation> {
    void login();

    void abortLogin();

    void logout();

    void onConnectivityChange();

    LoginError getLoginError();
    AutoregisteringError getAutoregisteringError();

    enum Operation {
        AUTO_REGISTERING,
        LOGIN
    }


    enum LoginError {
        INVALID_LOGIN,
        FAILED,
        UPGRADE_NEEDED
    }


    enum AutoregisteringError {
        FAILED,
        UPGRADE_NEEDED
    }
}
