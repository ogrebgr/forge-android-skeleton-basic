package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;


/**
 * Created by ogre on 2015-12-11 21:44
 */
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

    ResponseCodes.Errors getLastError();
}
