package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;


public interface Res_ScreenName {
    enum State {
        IDLE,
        PROCESSING,
        SCREEN_NAME_OK,
        SCREEN_NAME_FAIL
    }

    State getState();
    void screenName(String screenName);
    void resetState();

    ResponseCodes.Errors getLastError();
}