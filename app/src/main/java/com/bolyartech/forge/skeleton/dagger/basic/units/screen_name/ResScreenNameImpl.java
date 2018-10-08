package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.AbstractRctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.base.rc_task.RcTaskToExecutor;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class ResScreenNameImpl extends AbstractRctResidentComponent implements ResScreenName {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    private final Provider<ScreenNameTask> screenNameTaskProvider;

    private ScreenNameTask screenNameTask;

    private int lastError;

    @Inject
    public ResScreenNameImpl(RcTaskExecutor taskExecutor, Provider<ScreenNameTask> screenNameTaskProvider) {
        super(taskExecutor);
        this.screenNameTaskProvider = screenNameTaskProvider;
    }


    @Override
    public void screenName(String screenName) {
        screenNameTask = screenNameTaskProvider.get();
        screenNameTask.init(screenName);
        executeTask(screenNameTask);
    }


    @Override
    public int getLastError() {
        return lastError;
    }


    @Override
    protected void onTaskPostExecute(@NonNull RcTaskToExecutor endedTask) {
        if (screenNameTask.isFailure()) {
            lastError = screenNameTask.getResult().getErrorValue();
        }
    }
}
