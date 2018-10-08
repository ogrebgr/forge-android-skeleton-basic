package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.AbstractRctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.base.rc_task.RcTaskResult;
import com.bolyartech.forge.base.rc_task.RcTaskToExecutor;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class ResLoginImpl extends AbstractRctResidentComponent implements ResLogin {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Provider<LoginTask> loginTaskProvider;

    private RcTaskResult<Void, Integer> loginTaskResult;

    private LoginTask loginTask;

    @Inject
    public ResLoginImpl(RcTaskExecutor taskExecutor, Provider<LoginTask> loginTaskProvider) {

        super(taskExecutor);
        this.loginTaskProvider = loginTaskProvider;
    }


    @Override
    public void login(String username, String password) {
        if (isIdle()) {
            loginTask = loginTaskProvider.get();
            loginTask.init(username, password, false);
            executeTask(loginTask);
        }
    }


    @Override
    public RcTaskResult<Void, Integer> getLoginTaskResult() {
        return loginTaskResult;
    }


    @Override
    protected void onTaskPostExecute(@NonNull RcTaskToExecutor task) {
        loginTaskResult = loginTask.getResult();
    }
}
