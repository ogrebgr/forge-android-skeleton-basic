package com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


public interface ResLoginFacebook extends OperationResidentComponent, ForgeExchangeManagerListener {
    void checkFbLogin(String token);
}
