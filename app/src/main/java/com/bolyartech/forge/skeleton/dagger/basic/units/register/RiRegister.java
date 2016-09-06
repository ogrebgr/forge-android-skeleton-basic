package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.SeorcActivityInterface;


public interface RiRegister extends SeorcActivityInterface<Void, Integer> {
    void register(String username, String password, String screenName);
}
