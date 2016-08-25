package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.app_unit.IntOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface Res_ScreenName extends IntOperationResidentComponent, ForgeExchangeManagerListener {
    void screenName(String screenName);
}
