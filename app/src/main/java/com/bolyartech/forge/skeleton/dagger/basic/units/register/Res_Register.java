package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;


public interface Res_Register {
    enum State {
        IDLE,
        REGISTERING,
        REGISTER_OK,
        REGISTER_FAIL
    }


    void register(String username, String password, String screenName);
    State getState();
    void resetState();

    BasicResponseCodes.Errors getLastError();
}
