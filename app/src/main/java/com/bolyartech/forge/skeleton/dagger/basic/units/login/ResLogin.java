package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.task.RcTaskResult;


/**
 * Created by ogre on 2016-01-05 13:59
 */
public interface ResLogin extends RctResidentComponent {
    void login(String username, String password);

    RcTaskResult<Void, Integer> getLoginTaskResult();
    void abortLogin();
}
