package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.AbstractRctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.base.rc_task.RcTaskToExecutor;

import javax.inject.Inject;
import javax.inject.Provider;


public class ResRegisterImpl extends AbstractRctResidentComponent implements ResRegister {
    private final Provider<RegistrationTask> registrationTaskProvider;


    private RegistrationTask registrationTask;
    private int lastError;


    @Inject
    public ResRegisterImpl(RcTaskExecutor taskExecutor, Provider<RegistrationTask> registrationTaskProvider) {
        super(taskExecutor);
        this.registrationTaskProvider = registrationTaskProvider;
    }


    @Override
    public void register(String username, String password, String screenName) {
        registrationTask = registrationTaskProvider.get();
        registrationTask.init(username, password, screenName);
        executeTask(registrationTask);
    }


    @Override
    public int getLastError() {
        return lastError;
    }


    @Override
    public synchronized void abort() {
        super.abort();
    }


    @Override
    protected void onTaskPostExecute(@NonNull RcTaskToExecutor endedTask) {
        if (registrationTask.isFailure()) {
            lastError = registrationTask.getResult().getErrorValue();
        }
    }
}


