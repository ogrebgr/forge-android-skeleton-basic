package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;


/**
 * Created by ogre on 2015-10-05
 */
public interface Res_Main extends MultiOperationResidentComponent<Res_Main.Operation> {
    void login();

    void abortLogin();

    void logout();

    void onConnectivityChange();

    LoginResult getLoginResult();
    AutoregisteringResult getAutoregisteringResult();

    enum Operation {
        AUTO_REGISTERING,
        LOGIN
    }


    enum LoginResult {
        OK,
        INVALID_LOGIN,
        FAILED,
        UPGRADE_NEEDED
    }


    enum AutoregisteringResult {
        OK,
        FAILED,
        UPGRADE_NEEDED
    }
}
