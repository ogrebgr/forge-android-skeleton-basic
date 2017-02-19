package com.bolyartech.forge.skeleton.dagger.basic.app;


import android.os.Handler;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.ResidentComponent;


abstract public class OpSessionActivity<T extends ResidentComponent> extends SessionActivity<T>
        implements OperationResidentComponent.Listener {

    private final Handler mHandler = new Handler();

    protected abstract void handleState();


    @Override
    public void onResidentOperationStateChanged() {
        mHandler.post(this::handleState);
    }
}
