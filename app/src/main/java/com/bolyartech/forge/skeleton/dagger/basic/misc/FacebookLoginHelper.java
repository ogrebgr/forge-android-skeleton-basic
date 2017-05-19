package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;


public interface FacebookLoginHelper {
    void abortLogin();
    void checkFbLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token);


    interface Listener {
        void onFacebookLoginOk();

        void onFacebookLoginFail(int code);
    }
}
