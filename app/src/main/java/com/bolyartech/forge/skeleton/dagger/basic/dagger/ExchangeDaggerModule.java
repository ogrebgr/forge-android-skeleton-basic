package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.android.task.ForgeAndroidTaskExecutor;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.base.task.ForgeTaskExecutor;
import com.bolyartech.forge.base.task.TaskExecutor;
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
    private final ForgeExchangeManager mForgeExchangeManager;
    private final ForgeAndroidTaskExecutor mForgeAndroidTaskExecutor;


    public ExchangeDaggerModule(String baseUrl,
                                ForgeExchangeManager forgeExchangeManager,
                                ForgeAndroidTaskExecutor forgeAndroidTaskExecutor) {
        super();
        mBaseUrl = baseUrl;
        mForgeExchangeManager = forgeExchangeManager;
        mForgeAndroidTaskExecutor = forgeAndroidTaskExecutor;
    }


    @Provides
    @Named("base url")
    String provideBaseUrl() {
        return mBaseUrl;
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager() {
        return mForgeExchangeManager;
    }


    @Provides
    @Singleton
    @Named("forge result producer")
    public ResultProducer provideForgeGsonResultProducer(ForgeGsonResultProducer rp) {
        return rp;
    }


    @Provides
    @Singleton
    public ForgeAndroidTaskExecutor provideTaskExecutor() {
        return mForgeAndroidTaskExecutor;
    }
}
