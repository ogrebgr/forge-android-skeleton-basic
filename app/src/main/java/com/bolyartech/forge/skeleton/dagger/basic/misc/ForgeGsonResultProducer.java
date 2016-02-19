package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.exchange.ResultProducer;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.slf4j.LoggerFactory;


/**
 * Created by ogre on 2015-11-02 09:04
 */
public class ForgeGsonResultProducer implements ResultProducer<ForgeExchangeResult> {
    private Class<ForgeExchangeResult> mClass = ForgeExchangeResult.class;
    private Gson mGson = new Gson();

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    @Override
    public ForgeExchangeResult produce(String str) throws ResultProducerException {
        try {
            return mGson.fromJson(str, mClass);
        } catch (JsonSyntaxException e) {
            throw new ResultProducerException("Cannot parse JSON");
        }
    }
}
