package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;


public interface  Res_Register extends StatefulResidentComponent<Res_Register.State> {
    void register(String username, String password, String screenName);

    BasicResponseCodes.Errors getLastError();


    enum State {
        IDLE,
        REGISTERING,
        REGISTER_OK,
        REGISTER_FAIL;
    }
}