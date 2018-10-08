package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;


public interface ResRegister extends RctResidentComponent {
    void register(String username, String password, String screenName);

    int getLastError();
}