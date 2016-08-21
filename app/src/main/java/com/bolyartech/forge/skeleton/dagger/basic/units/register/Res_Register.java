package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.SimpleOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface  Res_Register extends SimpleOperationResidentComponent, ForgeExchangeManagerListener {
    void register(String username, String password, String screenName);
}