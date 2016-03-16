package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.support.v7.app.AppCompatActivity;

import com.bolyartech.forge.android.app_unit.UnitActivity;
import com.bolyartech.forge.android.mvp.Presenter;
import com.bolyartech.forge.android.app_unit.ResidentComponent;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity extends AppCompatActivity implements UnitActivity {
    private ResidentComponent mResidentComponent;


    @Override
    public void setResidentComponent(ResidentComponent res) {
        mResidentComponent = res;
    }


    @Override
    public ResidentComponent getResidentComponent() {
        return mResidentComponent;
    }
}
