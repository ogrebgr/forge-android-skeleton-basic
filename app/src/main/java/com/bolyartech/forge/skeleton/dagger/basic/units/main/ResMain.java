package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;


/**
 * Created by ogre on 2015-10-05
 */
public interface ResMain extends MultiOperationResidentComponent<ResMain.Operation> {


    enum Operation {
        AUTO_REGISTERING,
        LOGIN,
        FACEBOOK_LOGIN,
        LOGOUT
    }


    enum AutoregisteringError {
        FAILED,
        UPGRADE_NEEDED
    }

    void autoLoginIfNeeded();

    void login();

    void abortLogin();

    void logout();

    int getLoginError();

    ResMain.AutoregisteringError getAutoregisteringError();

    CurrentUser getCurrentUser();
}
