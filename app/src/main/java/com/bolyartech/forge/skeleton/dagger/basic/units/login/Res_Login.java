package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import com.bolyartech.forge.android.app_unit.IntOperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;


/**
 * Created by ogre on 2016-01-05 13:59
 */
public interface Res_Login extends IntOperationResidentComponent, ForgeExchangeManagerListener {
    void login(String username, String password);
    void abortLogin();
}
