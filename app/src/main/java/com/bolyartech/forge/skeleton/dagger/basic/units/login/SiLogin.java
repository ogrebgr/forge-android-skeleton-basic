package com.bolyartech.forge.skeleton.dagger.basic.units.login;


import com.bolyartech.forge.android.app_unit.SideEffectStateInterface;


public interface SiLogin extends SideEffectStateInterface<Void, Integer> {
    void login(String username, String password);
    void abortLogin();
}
