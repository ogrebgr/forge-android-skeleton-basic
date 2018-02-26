package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.TaskExecutionStateful;
import com.bolyartech.forge.android.app_unit.rc_task.activity.RctActivity;
import com.bolyartech.forge.android.app_unit.rc_task.activity.RctActivityDelegate;
import com.bolyartech.forge.android.app_unit.rc_task.activity.RctActivityDelegateImpl;
import com.bolyartech.forge.android.misc.ActivityResult;
import com.bolyartech.forge.android.misc.RunOnUiThreadHelper;

import javax.inject.Inject;


abstract public class RctUnitActivity<T extends RctResidentComponent & TaskExecutionStateful>
        extends UnitBaseActivity<T>
        implements RctResidentComponent.Listener, RctActivity {


    @Inject
    RunOnUiThreadHelper runOnUiThreadHelper;

    private RctActivityDelegate delegate;


    @Override
    public void onResidentTaskExecutionStateChanged() {
        delegate.onResidentTaskExecutionStateChanged();
    }


    @Override
    public void handleActivityResult(ActivityResult activityResult) {
        // empty to free the user of declaring it in every activity even when he does not care about the ActivityResult
    }


    public boolean isActivityJustCreated() {
        return delegate.isActivityJustCreated();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate = new RctActivityDelegateImpl(this, runOnUiThreadHelper);
    }


    @Override
    protected void onResume() {
        super.onResume();
        delegate.onResume();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        delegate.onActivityResult(requestCode, resultCode, data);
    }
}
