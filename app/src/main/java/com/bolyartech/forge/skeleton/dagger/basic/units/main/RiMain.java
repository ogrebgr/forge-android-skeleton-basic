package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MorcActivityInterface;


public interface RiMain extends MorcActivityInterface<ResMain.Operation> {
    void login();

    void abortLogin();

    void logout();

    void onConnectivityChange();

    ResMain.LoginError getLoginError();

    ResMain.AutoregisteringError getAutoregisteringError();
}
