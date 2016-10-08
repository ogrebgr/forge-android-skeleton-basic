package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.SideEffectOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface ResRegister extends SideEffectOperationResidentComponent<Void, Integer>,
        ForgeExchangeManagerListener {

    void register(String username, String password, String screenName);
}