package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;

import com.bolyartech.forge.android.app_unit.ResidentComponentState;
import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.END;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.START;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.TRANSIENT;


/**
 * Created by ogre on 2015-10-05
 */
public interface Res_Main extends StatefulResidentComponent<Res_Main.State> {
    State getState();

    void login();

    void startSession();

    void abortLogin();

    void logout();

    void internetAvailable();

    void resetState();

    void onConnectivityChange();

    boolean isJustAutoregistered();

    enum State implements ResidentComponentState {
        IDLE(START),
        AUTO_REGISTERING(TRANSIENT),
        LOGGING_IN(TRANSIENT),
        STARTING_SESSION(TRANSIENT),
        SESSION_STARTED_OK(END),
        SESSION_START_FAIL(END),
        LOGIN_INVALID(END),
        LOGIN_FAIL(END),
        UPGRADE_NEEDED(END),
        REGISTER_AUTO_FAIL(END);

        private final Type mType;


        State(Type type) {
            mType = type;
        }


        @Override
        public Type getType() {
            return mType;
        }
    }
}
