package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.http.functionality.HttpFunctionalityWCookies;
import com.bolyartech.forge.task.ForgeExchangeManager;
import com.bolyartech.forge.task.TaskExecutor;
import com.bolyartech.forge.task.TaskExecutorImpl;

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
    public TaskExecutor provideTaskExecutor(TaskExecutorImpl impl) {
        return impl;
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager(TaskExecutor te, HttpFunctionalityWCookies httpFunc) {
        return new ForgeExchangeManager(te, httpFunc);
    }
}
