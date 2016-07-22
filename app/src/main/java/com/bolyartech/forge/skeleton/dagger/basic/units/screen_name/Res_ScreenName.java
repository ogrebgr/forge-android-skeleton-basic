package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.ResidentComponentState;
import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;

import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.END;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.START;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.TRANSIENT;


public interface Res_ScreenName extends StatefulResidentComponent<Res_ScreenName.State> {
    enum State implements ResidentComponentState {
        IDLE(START),
        PROCESSING(TRANSIENT),
        SCREEN_NAME_OK(END),
        SCREEN_NAME_FAIL(END);

        private final Type mType;


        State(Type type) {
            mType = type;
        }


        public Type getType() {
            return mType;
        }
    }

    State getState();
    void screenName(String screenName);
    void stateHandled();

    BasicResponseCodes.Errors getLastError();
}
