package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.app_unit.AbstractResidentComponent;
import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeManager;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.misc.AndroidEventPoster;
import com.bolyartech.forge.misc.NetworkInfoProvider;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GsonResultProducer;
import com.squareup.otto.Bus;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 16:22
 */
abstract public class SessionResidentComponent extends AbstractResidentComponent {
    private final AndroidEventPoster mAndroidEventPoster = new AndroidEventPoster();

    @Inject
    @Named("base url")
    String mBaseUrl;

    @Inject
    Session mSession;

    @Inject
    ForgeExchangeManager mExchangeManager;

    @Inject
    NetworkInfoProvider mNetworkInfoProvider;


    @Inject
    Bus mBus;


    public static boolean isNeedLogin(ExchangeOutcome<ForgeExchangeResult> out) {
        return SessionImpl.isNeedLogin(out);
    }


    public Session getSession() {
        return mSession;
    }


    public ForgeExchangeManager getForgeExchangeManager() {
        return mExchangeManager;
    }


    public NetworkInfoProvider getNetworkInfoProvider() {
        return mNetworkInfoProvider;
    }


    protected void postEvent(MyAppEvent ev) {
        mAndroidEventPoster.postEvent(mBus, ev);
    }


    protected ForgeExchangeBuilder createForgeExchangeBuilder(String endpoint){
        ForgeExchangeBuilder b = new ForgeExchangeBuilder();
        b.baseUrl(mBaseUrl);
        b.endpoint(endpoint);
        b.resultProducer(new GsonResultProducer());
        b.resultClass(ForgeExchangeResult.class);

        return b;
    }
}