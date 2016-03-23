package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.android.app_unit.AbstractResidentComponent;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ExchangeOutcome;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.http.HttpFunctionality;
import com.bolyartech.forge.base.task.ExchangeManager;
import com.bolyartech.forge.base.task.ForgeExchangeManager;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 16:22
 */
abstract public class SessionResidentComponent extends AbstractResidentComponent implements ExchangeManager.Listener<ForgeExchangeResult> {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final AppConfiguration mAppConfiguration;
    private final ForgeExchangeHelper mForgeExchangeHelper;
    private final Session mSession;
    private final NetworkInfoProvider mNetworkInfoProvider;
    private final AndroidEventPoster mAndroidEventPoster;


    public SessionResidentComponent(AppConfiguration appConfiguration,
                                    ForgeExchangeHelper forgeExchangeHelper,
                                    Session session,
                                    NetworkInfoProvider networkInfoProvider,
                                    AndroidEventPoster androidEventPoster) {

        mAppConfiguration = appConfiguration;
        mForgeExchangeHelper = forgeExchangeHelper;
        mSession = session;
        mNetworkInfoProvider = networkInfoProvider;
        mAndroidEventPoster = androidEventPoster;
    }


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
        mAndroidEventPoster.postEvent(ev);
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (isSuccess) {
            mSession.prolong();
            mLogger.debug("Forge exchange returned with code {}", result.getCode());
        }

        onSessionExchangeOutcome(exchangeId, isSuccess, result);
    }


    public ForgeExchangeManager getForgeExchangeManager() {
        return mForgeExchangeHelper.getExchangeManager();
    }


    protected ForgePostHttpExchangeBuilder createForgePostHttpExchangeBuilder(String endpoint) {
        return mForgeExchangeHelper.createForgePostHttpExchangeBuilder(endpoint);
    }
}