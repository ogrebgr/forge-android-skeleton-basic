package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;

import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;
import com.squareup.otto.Bus;

import javax.inject.Inject;


abstract public class SessionActivity extends UnitBaseActivity {
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


    @Override
    public void onStop() {
        super.onStop();
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
