package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MultiOprationStateInterface;


public interface SiMain extends MultiOprationStateInterface<ResMain.Operation> {
    void login();

    void abortLogin();

    void logout();

    void onConnectivityChange();

    ResMain.LoginError getLoginError();

    ResMain.AutoregisteringError getAutoregisteringError();
}
