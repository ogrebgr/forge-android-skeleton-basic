package com.bolyartech.forge.skeleton.dagger.basic.app;


import android.content.Intent;
import android.os.Handler;

import com.bolyartech.forge.android.app_unit.OpStateful;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.misc.ActivityResult;

import org.slf4j.LoggerFactory;


abstract public class OpSessionActivity<T extends ResidentComponent & OpStateful> extends SessionActivity<T>
        implements OperationResidentComponent.Listener {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());


    private final Handler mHandler = new Handler();

    private ActivityResult mActivityResult;

    private boolean mIsActivityJustCreated = true;


    @Override
    public void onResidentOperationStateChanged() {
        mHandler.post(this::handleState);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mActivityResult != null) {
            handleActivityResult(mActivityResult);
            mActivityResult = null;
        } else {
            handleState();
        }
    }


    public boolean isActivityJustCreated() {
        return mIsActivityJustCreated;
    }


    protected abstract void handleResidentIdleState();


    protected abstract void handleResidentBusyState();


    protected abstract void handleResidentEndedState();


    protected void handleActivityResult(ActivityResult activityResult) {
        // empty
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityResult = new ActivityResult(requestCode, resultCode, data);
    }


    protected synchronized void handleState() {
        mLogger.debug("{} state {}", this.getClass(), getRes().getOpState());

        switch (getRes().getOpState()) {
            case IDLE:
                runOnUiThread(() -> {
                    handleResidentIdleState();
                    mIsActivityJustCreated = false;
                });

                break;
            case BUSY:
                runOnUiThread(this::handleResidentBusyState);

                break;
            case ENDED:
                runOnUiThread(() -> {
                    handleResidentEndedState();
                    getRes().ack();
                });

                break;
        }
    }
}
