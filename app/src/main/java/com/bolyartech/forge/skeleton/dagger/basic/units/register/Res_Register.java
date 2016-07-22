package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.ResidentComponentState;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;

import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.END;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.START;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.TRANSIENT;


public interface  Res_Register extends ResidentComponent {
    enum State implements ResidentComponentState {
        IDLE(START),
        REGISTERING(TRANSIENT),
        REGISTER_OK(END),
        REGISTER_FAIL(END);

        private final Type mType;


        State(Type type) {
            mType = type;
        }


        @Override
        public Type getType() {
            return mType;
        }
    }


    void register(String username, String password, String screenName);
    State getState();
    void resetState();

    BasicResponseCodes.Errors getLastError();
}
