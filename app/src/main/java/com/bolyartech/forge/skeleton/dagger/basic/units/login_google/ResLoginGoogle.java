package com.bolyartech.forge.skeleton.dagger.basic.units.login_google;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface ResLoginGoogle extends OperationResidentComponent, ForgeExchangeManagerListener {
    void checkGoogleLogin(String token);
}
