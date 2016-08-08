package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.UnitActivity;

import javax.inject.Inject;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity<T extends ResidentComponent> extends BaseActivity implements UnitActivity<T> {
    private T mResidentComponent;


    @Override
    public void setResidentComponent(T res) {
        mResidentComponent = res;
    }


    @Override
    public T getResidentComponent() {
        return mResidentComponent;
    }
}
