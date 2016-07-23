package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;


/**
 * Created by ogre on 2015-10-05
 */
public interface Res_Main extends StatefulResidentComponent<Res_Main.State> {
    void login();

    void startSession();

    void abortLogin();

    void logout();

    void internetAvailable();

    void onConnectivityChange();

    boolean isJustAutoregistered();

    enum State {
        IDLE,
        AUTO_REGISTERING,
        LOGGING_IN,
        STARTING_SESSION,
        SESSION_STARTED_OK,
        SESSION_START_FAIL,
        LOGIN_INVALID,
        LOGIN_FAIL,
        UPGRADE_NEEDED,
        REGISTER_AUTO_FAIL;
    }
}
