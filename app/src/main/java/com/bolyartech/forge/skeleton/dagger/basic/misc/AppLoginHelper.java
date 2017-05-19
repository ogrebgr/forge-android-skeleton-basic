package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;


public interface AppLoginHelper {
    void initiate(
                  ForgePostHttpExchangeBuilder step1builder,
                  ForgePostHttpExchangeBuilder step2builder,
                  String username,
                  String password,
                  Listener listener,
                  boolean autologin);

    void abortLogin();


    interface Listener {
        void onLoginOk();

        void onLoginFail(int code);
    }
}
