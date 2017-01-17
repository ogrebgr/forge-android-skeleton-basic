package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;


public interface GoogleLoginHelper {
    void abortLogin();
    void checkGoogleLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token);
    boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result);


    interface Listener {
        void onGoogleLoginOk();

        void onGoogleLoginFail(int code);
    }

}
