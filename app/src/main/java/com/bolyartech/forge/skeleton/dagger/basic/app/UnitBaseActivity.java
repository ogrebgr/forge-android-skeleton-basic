package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.UnitActivity;


/**
 * Created by ogre on 2016-01-10 12:45
 */
abstract public class UnitBaseActivity<T>
        extends BaseActivity implements UnitActivity<T> {

    private T mResidentInterface;


    @Override
    public void setResidentToActivity(@NonNull T ri) {
        mResidentInterface = ri;
    }


    @NonNull
    @Override
    public T getResidentToActivity() {
        return mResidentInterface;
    }


    @NonNull
    @Override
    public T getRta() {
        return getResidentToActivity();
    }
}
