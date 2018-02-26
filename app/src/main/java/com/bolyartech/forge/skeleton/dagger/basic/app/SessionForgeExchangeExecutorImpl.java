package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.session.Session;

import java.io.IOException;

import javax.inject.Inject;


public class SessionForgeExchangeExecutorImpl implements SessionForgeExchangeExecutor {
    private final Session session;


    @Inject
    public SessionForgeExchangeExecutorImpl(Session session) {
        this.session = session;
    }


    @Override
    public ForgeExchangeResult execute(HttpExchange<ForgeExchangeResult> x) throws IOException,
            ResultProducer.ResultProducerException {

        ForgeExchangeResult ret = x.execute();
        session.prolong();
        return ret;
    }
}
