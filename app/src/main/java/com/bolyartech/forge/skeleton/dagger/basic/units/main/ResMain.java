package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;



public interface ResMain extends MultiOperationResidentComponent<ResMain.Operation> {


    void autoLoginIfNeeded();

    void login();

    void abortLogin();

    void logout();

    int getLoginError();

    ResMain.AutoregisteringError getAutoregisteringError();

    CurrentUser getCurrentUser();

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
}
