package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.http.HttpFunctionality;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.base.task.ForgeTaskExecutor;
import com.bolyartech.forge.base.task.TaskExecutor;
import com.bolyartech.forge.base.task.TaskExecutorImpl;
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForgeGsonResultProducer;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module(includes = {HttpsDaggerModule.class})
public class ExchangeDaggerModule {
    private final String mBaseUrl;


    public ExchangeDaggerModule(String baseUrl) {
        super();
        mBaseUrl = baseUrl;
    }


    @Provides
    @Named("base url")
    String provideBaseUrl() {
        return mBaseUrl;
    }



    @Provides
    @Singleton
    public ForgeTaskExecutor provideTaskExecutor() {
        return new ForgeTaskExecutor();
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager(ForgeTaskExecutor te) {
        return new ForgeExchangeManager(te);
    }

    @Provides
    @Singleton
    @Named("forge result producer")
    public ResultProducer provideForgeGsonResultProducer(ForgeGsonResultProducer rp) {
        return rp;
    }
}
