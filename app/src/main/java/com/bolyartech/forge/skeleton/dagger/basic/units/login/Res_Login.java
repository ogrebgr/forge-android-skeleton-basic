package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;


/**
 * Created by ogre on 2016-01-05 13:59
 */
public interface Res_Login extends StatefulResidentComponent<Res_Login.State> {
    void login(String username, String password);
    void abortLogin();
    BasicResponseCodes.Errors getLastError();


    enum State {
        IDLE,
        LOGGING_IN,
        LOGIN_FAIL,
        STARTING_SESSION,
        SESSION_STARTED_OK,
        SESSION_START_FAIL;
    }
}
