package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;

import com.bolyartech.forge.app_unit.ActivityComponentImpl;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;

import javax.inject.Inject;


abstract public class SessionActivityComponent extends ActivityComponentImpl {
    @Inject
    Session mSession;


    public Session getSession() {
        return mSession;
    }


    protected MyAppDaggerComponent getDependencyInjector() {
        return ((MyApp) getApplication()).getDependencyInjector();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (!(this instanceof DoesLogin)) {
            if (!mSession.isLoggedIn()) {
                goHome();
            }
        }
    }


    public void goHome() {
        Intent intent = new Intent(getApplicationContext(), Act_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
