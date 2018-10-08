package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.rc_task.RctResidentComponent;


public interface ResScreenName extends RctResidentComponent {
    void screenName(String screenName);

    void abort();

    int getLastError();
}
