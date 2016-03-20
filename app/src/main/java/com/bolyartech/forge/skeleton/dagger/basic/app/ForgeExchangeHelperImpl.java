package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.http.HttpFunctionality;
import com.bolyartech.forge.base.task.ForgeExchangeManager;

import javax.inject.Inject;
import javax.inject.Named;


public class ForgeExchangeHelperImpl implements ForgeExchangeHelper {
    private final ForgeExchangeManager mExchangeManager;

    private final HttpFunctionality mHttpFunctionality;

    private final ResultProducer<ForgeExchangeResult> mResultProducer;

    private final String mBaseUrl;


    @Inject
    public ForgeExchangeHelperImpl(ForgeExchangeManager exchangeManager,
                                   HttpFunctionality httpFunctionality,
                                   ResultProducer<ForgeExchangeResult> resultProducer,
                                   @Named("base url") String baseUrl) {

        mExchangeManager = exchangeManager;
        mHttpFunctionality = httpFunctionality;
        mResultProducer = resultProducer;
        mBaseUrl = baseUrl;
    }


    @Override
    public ForgePostHttpExchangeBuilder createForgePostHttpExchangeBuilder(String endpoint) {
        return new ForgePostHttpExchangeBuilder(mHttpFunctionality, mResultProducer, mBaseUrl + endpoint);
    }


    @Override
    public ForgeExchangeManager getExchangeManager() {
        return mExchangeManager;
    }
}
