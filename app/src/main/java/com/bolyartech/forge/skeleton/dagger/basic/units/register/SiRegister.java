package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.SideEffectStateInterface;


public interface SiRegister extends SideEffectStateInterface<Void, Integer> {
    void register(String username, String password, String screenName);
}
