package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.base.rc_task.RcTask;
import com.bolyartech.forge.base.rc_task.RcTaskResult;


public interface AutoregisterTask extends RcTask<RcTaskResult<Void, Integer>> {
    int TASK_ID = 3;
}

