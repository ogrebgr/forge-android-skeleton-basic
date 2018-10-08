package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.AbstractRctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.rc_task.RcTaskToExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.LoginTask;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class ResMainImpl extends AbstractRctResidentComponent implements ResMain {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppConfiguration appConfiguration;
    private final NetworkInfoProvider mNetworkInfoProvider;
    private final CurrentUserHolder currentUserHolder;
    private final Provider<LogoutTask> logoutTaskProvider;
    private final Provider<AutoregisterTask> autoregisterTaskProvider;
    private final Provider<LoginTask> loginTaskProvider;


    private AutoregisterTask autoregisterTask;
    private LoginTask loginTask;


    private int autoregisterError;
    private int loginError;


    @Inject
    public ResMainImpl(RcTaskExecutor taskExecutor,
                       AppConfiguration appConfiguration,
                       NetworkInfoProvider networkInfoProvider,
                       CurrentUserHolder currentUserHolder,
                       Provider<LogoutTask> logoutTaskProvider,
                       Provider<AutoregisterTask> autoregisterTaskProvider, Provider<LoginTask> loginTaskProvider) {

        super(taskExecutor);


        this.appConfiguration = appConfiguration;
        mNetworkInfoProvider = networkInfoProvider;
        this.currentUserHolder = currentUserHolder;
        this.logoutTaskProvider = logoutTaskProvider;
        this.autoregisterTaskProvider = autoregisterTaskProvider;
        this.loginTaskProvider = loginTaskProvider;
    }


    public int getAutoregisterError() {
        return autoregisterError;
    }


    @Override
    public CurrentUser getCurrentUser() {
        return currentUserHolder.getCurrentUser();
    }


    @Override
    public void autoLoginIfNeeded() {
        if (mNetworkInfoProvider.isConnected()) {
            if (appConfiguration.getLoginPrefs().hasLoginCredentials()) {
                loginActual();
            } else {
                if (appConfiguration.shallAutoregister()) {
                    autoRegister();
                }
            }
        }
    }


    @Override
    public void login() {
        loginActual();
    }


    @Override
    public synchronized void abort() {
        logger.debug("abort");
        super.abort();
        if (loginTask != null) {
            loginTask.cancel();
        }
    }


    @Override
    public void logout() {
        executeTask(logoutTaskProvider.get());
    }


    @Override
    public int getLoginError() {
        return loginError;
    }


    @Override
    protected void onTaskPostExecute(@NonNull RcTaskToExecutor endedTask) {
        if (endedTask.getId() == AutoregisterTask.TASK_ID) {
            handleAutoregisterTaskEnded();
        } else if (endedTask.getId() == LoginTask.TASK_ID) {
            handleLoginTaskEnded();
        }
    }


    private void handleLoginTaskEnded() {
        if (!loginTask.isSuccess()) {
            logger.warn("Login task failed");
            loginError = loginTask.getResult().getErrorValue();
        }
    }


    private void handleAutoregisterTaskEnded() {
        if (!autoregisterTask.isSuccess()) {
            logger.warn("Register auto task failed");
            autoregisterError = autoregisterTask.getResult().getErrorValue();
        }
    }


    private void autoRegister() {
        autoregisterTask = autoregisterTaskProvider.get();
        executeTask(autoregisterTask);
    }


    private void loginActual() {
        loginTask = loginTaskProvider.get();
        loginTask.init(appConfiguration.getLoginPrefs().getUsername(),
                appConfiguration.getLoginPrefs().getPassword(),
                true);
        executeTask(loginTask);
    }
}
