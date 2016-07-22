package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;


import com.bolyartech.forge.android.app_unit.ResidentComponentState;
import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;

import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.END;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.START;
import static com.bolyartech.forge.android.app_unit.ResidentComponentState.Type.TRANSIENT;


/**
 * Created by ogre on 2016-01-06 21:10
 */
public interface Res_SelectLogin extends StatefulResidentComponent<Res_SelectLogin.State> {
    enum State implements ResidentComponentState {
        IDLE(START),
        WAITING_FB_CHECK(TRANSIENT),
        FB_CHECK_OK(END),
        FB_CHECK_FAIL(END),
        WAITING_GOOGLE_CHECK(TRANSIENT),
        GOOGLE_CHECK_OK(END),
        GOOGLE_CHECK_FAIL(END);

        private final Type mType;


        State(Type type) {
            mType = type;
        }


        @Override
        public Type getType() {
            return mType;
        }
    }

    State getState();

    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

    void logout();
}

