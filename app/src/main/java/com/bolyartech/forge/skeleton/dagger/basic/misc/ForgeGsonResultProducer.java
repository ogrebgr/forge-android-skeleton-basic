//package com.bolyartech.forge.skeleton.dagger.basic.misc;
//
//import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
//import com.bolyartech.forge.base.exchange.ResultProducer;
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//
//import javax.inject.Inject;
//
//import okhttp3.Response;
//
//
///**
// * Created by ogre on 2015-11-02 09:04
// */
//public class ForgeGsonResultProducer implements ResultProducer<ForgeExchangeResult> {
//    private Gson mGson = new Gson();
//
//
//    @Inject
//    public ForgeGsonResultProducer() {
//    }
//
//
//    @Override
//    public ForgeExchangeResult produce(Response resp) throws ResultProducerException {
//        try {
//            return mGson.fromJson(resp.body().string(), ForgeExchangeResult.class);
//        } catch (JsonSyntaxException e) {
//            throw new ResultProducerException("Cannot parse JSON.", e);
//        } catch (IOException e) {
//            throw new ResultProducerException("Error getting response body.", e);
//        }
//    }
//}
