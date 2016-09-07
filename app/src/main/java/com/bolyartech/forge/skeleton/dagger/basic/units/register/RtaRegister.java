package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.SideEffectOperationResidentToActivity;


public interface RtaRegister extends SideEffectOperationResidentToActivity<Void, Integer> {
    void register(String username, String password, String screenName);
}
