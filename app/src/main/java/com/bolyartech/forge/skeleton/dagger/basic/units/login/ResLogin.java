package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;
import com.bolyartech.forge.base.rc_task.RcTaskResult;


public interface ResLogin extends RctResidentComponent {
    void login(String username, String password);

    RcTaskResult<Void, Integer> getLoginTaskResult();
}
