package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;

import java.io.IOException;


public interface SessionForgeExchangeExecutor {
    ForgeExchangeResult execute(HttpExchange<ForgeExchangeResult> x) throws IOException, ResultProducer.ResultProducerException;
}
