package com.bolyartech.forge.skeleton.dagger.basic.units.login;


import com.bolyartech.forge.android.app_unit.SideEffectOperationResidentToActivity;


public interface RtaLogin extends SideEffectOperationResidentToActivity<Void, Integer> {
    void login(String username, String password);
    void abortLogin();
}
