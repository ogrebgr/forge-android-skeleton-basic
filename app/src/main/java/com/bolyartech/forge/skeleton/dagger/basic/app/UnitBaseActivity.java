package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.UnitActivity;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity<T extends ResidentComponent> extends BaseActivity implements UnitActivity<T> {
    private T mResidentComponent;


    @Override
    public void setResident(@NonNull T res) {
        mResidentComponent = res;
    }


    @NonNull
    @Override
    public T getResidentComponent() {
        return mResidentComponent;
    }


    @NonNull
    @Override
    public T getResident() {
        return getResidentComponent();
    }


    @NonNull
    @Override
    public T getRes() {
        return getResidentComponent();
    }
}
