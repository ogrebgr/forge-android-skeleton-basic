package com.bolyartech.forge.skeleton.dagger.basic.units.login;


import com.bolyartech.forge.android.app_unit.SeorcActivityInterface;


public interface RiLogin extends SeorcActivityInterface<Void, Integer> {
    void login(String username, String password);
    void abortLogin();
}
