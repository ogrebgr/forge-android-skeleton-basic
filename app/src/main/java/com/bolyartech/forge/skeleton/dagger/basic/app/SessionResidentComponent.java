package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.android.app_unit.AbstractResidentComponent;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.misc.GsonResultProducer;
import com.bolyartech.forge.task.ExchangeManager;
import com.bolyartech.forge.task.ForgeExchangeManager;
import com.squareup.otto.Bus;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 16:22
 */
abstract public class SessionResidentComponent extends AbstractResidentComponent implements ExchangeManager.Listener<ForgeExchangeResult> {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AndroidEventPoster mAndroidEventPoster = new AndroidEventPoster();

    @Inject
    @Named("base url")
    String mBaseUrl;

    @Inject
    Session mSession;

    @Inject
    NetworkInfoProvider mNetworkInfoProvider;

    @Inject
    Bus mBus;

    @Inject
    ForgeExchangeManager mExchangeManager;


    abstract public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result);


    public static boolean isNeedLogin(ExchangeOutcome<ForgeExchangeResult> out) {
        return SessionImpl.isNeedLogin(out);
    }


    public Session getSession() {
        return mSession;
    }


    public NetworkInfoProvider getNetworkInfoProvider() {
        return mNetworkInfoProvider;
    }


    protected void postEvent(MyAppEvent ev) {
        mAndroidEventPoster.postEvent(mBus, ev);
    }


    protected ForgeExchangeBuilder createForgeExchangeBuilder(String endpoint) {
        ForgeExchangeBuilder b = new ForgeExchangeBuilder();
        b.baseUrl(mBaseUrl);
        b.endpoint(endpoint);
        b.resultProducer(new GsonResultProducer());
        b.resultClass(ForgeExchangeResult.class);

        return b;
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        mSession.prolong();
        mLogger.debug("Forge exchange returned with code {}", result.getCode());
        onSessionExchangeOutcome(exchangeId, isSuccess, result);
    }


    public ForgeExchangeManager getForgeExchangeManager() {
        return mExchangeManager;
    }
}