package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.support.annotation.NonNull;

import com.bolyartech.forge.base.rc_task.RcTask;
import com.bolyartech.forge.base.rc_task.RcTaskResult;


public interface LoginTask extends RcTask<RcTaskResult<Void, Integer>> {
    int TASK_ID = 1;

    void init(@NonNull String username, @NonNull String password, boolean autologin);
}
