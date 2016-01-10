package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.app_unit.ActivityComponent;
import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.app_unit.UnitManager;

import javax.inject.Inject;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity extends BaseActivity implements ActivityComponent {

    @Inject
    UnitManager mUnitManager;

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
