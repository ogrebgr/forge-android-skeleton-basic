package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;


public interface Res_ScreenName extends StatefulResidentComponent<Res_ScreenName.State> {
    void screenName(String screenName);
    void stateHandled();

    BasicResponseCodes.Errors getLastError();

    enum State {
        IDLE,
        PROCESSING,
        SCREEN_NAME_OK,
        SCREEN_NAME_FAIL;
    }
}
