package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.ResultProducer;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Response;


public class ForgePrefixResultProducer implements ResultProducer<ForgeExchangeResult> {
    private static final String DELIMITER = "#";


    @Inject
    public ForgePrefixResultProducer() {
    }


    @Override
    public ForgeExchangeResult produce(Response resp) throws ResultProducerException {
        try {
            String body = resp.body().string();
            int pos = body.indexOf(DELIMITER);
            if (pos > 0) {
                if (body.length() > pos) {
                    return new ForgeExchangeResult(Integer.valueOf(body.substring(0, pos)), body.substring(pos + 1));
                } else {
                    return new ForgeExchangeResult(Integer.valueOf(body.substring(0, pos)), "");
                }
            } else {
                throw new ResultProducerException("Cannot find delimiter " + DELIMITER);
            }
        } catch (NumberFormatException e) {
            throw new ResultProducerException("Non integer code");
        } catch (IOException e) {
            throw new ResultProducerException("Error getting response body.", e);
        }
    }
}
