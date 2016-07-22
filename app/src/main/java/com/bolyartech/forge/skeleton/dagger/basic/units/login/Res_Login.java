package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.ResidentComponentState;
import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;

import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.END;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.START;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.TRANSIENT;


/**
 * Created by ogre on 2016-01-05 13:59
 */
public interface Res_Login extends StatefulResidentComponent<Res_Login.State> {
    enum State implements ResidentComponentState {
        IDLE(START),
        LOGGING_IN(TRANSIENT),
        LOGIN_FAIL(END),
        STARTING_SESSION(TRANSIENT),
        SESSION_STARTED_OK(END),
        SESSION_START_FAIL(END);

        private final ResidentComponentState.Type mType;


        State(ResidentComponentState.Type type) {
            mType = type;
        }


        public ResidentComponentState.Type getType() {
            return mType;
        }
    }


    State getState();
    void login(String username, String password);
    void abortLogin();
    BasicResponseCodes.Errors getLastError();
}
