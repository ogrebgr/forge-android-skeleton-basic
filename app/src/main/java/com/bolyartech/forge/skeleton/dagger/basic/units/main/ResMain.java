package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;


public interface ResMain extends RctResidentComponent {
    void autoLoginIfNeeded();

    void login();

    void logout();

    int getLoginError();

    int getAutoregisterError();

    CurrentUser getCurrentUser();

    enum Operation {
        AUTO_REGISTERING,
        LOGIN,
        FACEBOOK_LOGIN,
        LOGOUT
    }
}

