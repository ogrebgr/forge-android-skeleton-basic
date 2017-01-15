package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;


public interface FacebookLoginHelper {
    void abortLogin();
    void checkFbLogin(ForgePostHttpExchangeBuilder exchangeBuilder, Listener listener, String token);
    boolean handleExchange(long exchangeId, boolean isSuccess, ForgeExchangeResult result);


    interface Listener {
        void onFacebookLoginOk();

        void onFacebookLoginFail(int code);
    }
}
