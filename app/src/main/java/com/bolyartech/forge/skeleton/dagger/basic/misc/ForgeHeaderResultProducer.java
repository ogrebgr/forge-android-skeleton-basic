package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.ResultProducer;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Response;


public class ForgeHeaderResultProducer implements ResultProducer<ForgeExchangeResult> {
    private static final String FORGE_RESULT_CODE_HEADER = "X-Forge-Result-Code";


    @Inject
    public ForgeHeaderResultProducer() {
    }


    @Override
    public ForgeExchangeResult produce(Response resp) throws ResultProducerException {
        String codeStr = resp.header(FORGE_RESULT_CODE_HEADER);

        if (codeStr != null) {
            try {
                String body = resp.body().string();
                return new ForgeExchangeResult(Integer.valueOf(codeStr), body);
            } catch (NumberFormatException e) {
                throw new ResultProducerException("Non integer result code in header " +
                        FORGE_RESULT_CODE_HEADER + ": " + codeStr);
            } catch (IOException e) {
                throw new ResultProducerException("Error getting response body.", e);
            }
        } else {
            throw new ResultProducerException("Missing header " + FORGE_RESULT_CODE_HEADER);
        }
    }
}
