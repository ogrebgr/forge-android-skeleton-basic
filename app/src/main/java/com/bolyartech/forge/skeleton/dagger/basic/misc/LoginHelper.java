package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;


public interface LoginHelper {
    void initiate(
                  ForgePostHttpExchangeBuilder step1builder,
                  ForgePostHttpExchangeBuilder step2builder,
                  String username,
                  String password,
                  Listener listener,
                  boolean autologin);

    void abortLogin();
    boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result);


    interface Listener {
        void onLoginOk();

        void onInvalidLogin();

        void onUpgradeNeeded();

        void onLoginFail();
    }
}
