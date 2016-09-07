package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentToActivity;


public interface RtaMain extends MultiOperationResidentToActivity<ResMain.Operation> {
    void login();

    void abortLogin();

    void logout();

    void onConnectivityChange();

    ResMain.LoginError getLoginError();

    ResMain.AutoregisteringError getAutoregisteringError();
}
