package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;

import javax.inject.Inject;


abstract public class SessionActivity<T extends ResidentComponent>
        extends UnitBaseActivity<T> {

    @Inject
    Session mSession;


    @Override
    public void onResume() {
        super.onResume();

        if (!(this instanceof DoesLogin)) {
            if (!mSession.isLoggedIn()) {
                goHome();
            }
        }
    }


    public Session getSession() {
        return mSession;
    }


    public void goHome() {
        Intent intent = new Intent(getApplicationContext(), Act_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
