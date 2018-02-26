package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ActMain;

import javax.inject.Inject;


abstract public class SessionActivity<T extends ResidentComponent>
        extends UnitBaseActivity<T> {

    @Inject
    Session mSession;


    public Session getSession() {
        return mSession;
    }


    public void goHome() {
        Intent intent = new Intent(getApplicationContext(), ActMain.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!(this instanceof PerformsLogin)) {
            if (!mSession.isLoggedIn()) {
                goHome();
            }
        }
    }
}
