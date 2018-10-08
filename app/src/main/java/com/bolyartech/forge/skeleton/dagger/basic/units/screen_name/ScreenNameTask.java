package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.base.rc_task.failing.FailingRcTask;


public interface ScreenNameTask extends FailingRcTask<Integer> {
    int TASK_ID = 4;

    void init(String screenName);
}
