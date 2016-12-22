package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.android.task.ForgeAndroidTaskExecutor;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelperImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.forge.ForgeHeaderResultProducer;
import com.bolyartech.forge.base.http.HttpFunctionality;

import java.io.IOException;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Request;
import okhttp3.Response;


@Module
public class FakeExchangeDaggerModule {
    private final String mBaseUrl;


    public FakeExchangeDaggerModule(String baseUrl) {
        mBaseUrl = baseUrl;
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager() {
        return new ForgeExchangeManager();
    }


    @Provides
    @Singleton
    @Named("forge result producer")
    public ResultProducer<ForgeExchangeResult> provideForgeResultProducer(ForgeHeaderResultProducer rp) {
        return rp;
    }


    @Provides
    public ForgeAndroidTaskExecutor provideTaskExecutor() {
        return new ForgeAndroidTaskExecutor();
    }


    @Provides
    @Named("base url")
    String provideBaseUrl() {
        return mBaseUrl;
    }


    @Provides
    @Singleton
    ForgeExchangeHelper provideForgeExchangeHelper(ForgeExchangeHelperImpl impl) {
        return impl;
    }


    @Provides
    @Singleton
    HttpFunctionality providesHttpFunctionality() {
        return new HttpFunctionality() {
            @Override
            public Response execute(Request request) throws IOException {
                return new Response.Builder().build();
            }
        };
    }
}
