package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;


public interface GoogleLoginHelper {
    void abortLogin();
    void checkGoogleLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token);


    interface Listener {
        void onGoogleLoginOk();

        void onGoogleLoginFail(int code);
    }

}
