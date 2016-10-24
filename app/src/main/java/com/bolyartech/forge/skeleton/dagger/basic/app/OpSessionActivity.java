package com.bolyartech.forge.skeleton.dagger.basic.app;


import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.ResidentComponent;


abstract public class OpSessionActivity<T extends ResidentComponent> extends SessionActivity<T>
        implements OperationResidentComponent.Listener {


    protected abstract void handleState();


    @Override
    public void onResidentOperationStateChanged() {
        runOnUiThread(this::handleStatePrivate);
    }


    private synchronized void handleStatePrivate() {
        handleState();
    }
}
