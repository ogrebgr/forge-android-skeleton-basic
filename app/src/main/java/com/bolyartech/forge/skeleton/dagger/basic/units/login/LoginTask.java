package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.task.RcTask;
import com.bolyartech.forge.android.app_unit.rc_task.task.RcTaskResult;


public interface LoginTask extends RcTask<RcTaskResult<Void, Integer>> {
    void init(@NonNull String username, @NonNull String password, boolean autologin);
}
