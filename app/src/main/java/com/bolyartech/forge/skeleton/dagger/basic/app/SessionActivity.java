package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;

import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ActMain;

import javax.inject.Inject;


abstract public class SessionActivity<T>
        extends UnitBaseActivity<T> {

    @Inject
    Session mSession;


    @Override
    public void onResume() {
        super.onResume();

        if (!(this instanceof PerformsLogin)) {
            if (!mSession.isLoggedIn()) {
                goHome();
            }
        }
    }


    public Session getSession() {
        return mSession;
    }


    public void goHome() {
        Intent intent = new Intent(getApplicationContext(), ActMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
