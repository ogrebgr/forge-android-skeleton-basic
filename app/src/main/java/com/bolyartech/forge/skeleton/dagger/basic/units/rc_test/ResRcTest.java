package com.bolyartech.forge.skeleton.dagger.basic.units.rc_test;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.task.RcTaskResult;


public interface ResRcTest extends RctResidentComponent {
    void test1();

    void test2();

    void abort();

    RcTaskResult<String, Integer> getTask1Result();

    RcTaskResult<String, Void> getTask2Result();
}
