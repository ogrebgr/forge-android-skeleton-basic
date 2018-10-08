package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.base.rc_task.failing.FailingRcTask;


public interface RegistrationTask extends FailingRcTask<Integer> {
    int TASK_ID = 5;


    void init(String username, String password, String screenName);
}
