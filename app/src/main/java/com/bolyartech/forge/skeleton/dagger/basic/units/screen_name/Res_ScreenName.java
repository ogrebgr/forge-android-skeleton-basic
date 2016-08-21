package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.SimpleOperationResidentComponent;
import com.bolyartech.forge.base.exchange.ResponseCode;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface Res_ScreenName extends SimpleOperationResidentComponent, ForgeExchangeManagerListener {
    void screenName(String screenName);
}
