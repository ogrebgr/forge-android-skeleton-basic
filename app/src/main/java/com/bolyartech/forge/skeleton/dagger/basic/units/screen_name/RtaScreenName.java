package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.SideEffectOperationResidentToActivity;


public interface RtaScreenName extends SideEffectOperationResidentToActivity<Void, Integer> {
    void screenName(String screenName);
}
