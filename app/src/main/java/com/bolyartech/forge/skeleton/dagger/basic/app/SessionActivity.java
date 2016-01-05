package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.bolyartech.forge.app_unit.ActivityComponent;
import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.app_unit.UnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;
import com.squareup.otto.Bus;

import javax.inject.Inject;


abstract public class SessionActivity extends BaseActivity implements ActivityComponent {
    @Inject
    Session mSession;

    @Inject
    Bus mBus;


    @Inject
    UnitManager mUnitManager;


    private ResidentComponent mResidentComponent;


    @Override
    public void onResume() {
        super.onResume();
        mResidentComponent = mUnitManager.onActivityResumed(this);
        mBus.register(this);

        if (!(this instanceof DoesLogin)) {
            if (!mSession.isLoggedIn()) {
                goHome();
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mUnitManager.onActivityPaused(this);
        mBus.unregister(this);
    }


    @Override
    public void onStop() {
        super.onStop();
        mUnitManager.onActivityStop(this);
    }


    public Session getSession() {
        return mSession;
    }


    public void goHome() {
        Intent intent = new Intent(getApplicationContext(), Act_Main.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    @Override
    public ResidentComponent getResidentComponent() {
        return mResidentComponent;
    }
}
