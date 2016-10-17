package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.UnitActivity;
import com.bolyartech.forge.android.app_unit.UnitApplicationDelegate;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity<T extends ResidentComponent>
        extends BaseActivity implements UnitActivity<T> {

    private UnitApplicationDelegate<T> mDelegate = new UnitApplicationDelegate<>();


    @Override
    public void setResident(@NonNull T resident) {
        mDelegate.setResident(resident);
    }


    @Override
    @NonNull
    public T getResident() {
        return mDelegate.getResident();
    }


    @Override
    @NonNull
    public T getRes() {
        return mDelegate.getRes();
    }
}
