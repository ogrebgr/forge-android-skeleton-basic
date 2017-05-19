package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.task.ForgeAndroidTaskExecutor;
import com.bolyartech.forge.base.exchange.Exchange;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelperImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.forge.ForgeHeaderResultProducer;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@Module(includes = {HttpsDaggerModule.class})
public class AutoLogin_ExchangeDaggerModule {
    public AutoLogin_ExchangeDaggerModule() {
        super();
    }


    @Provides
    @Singleton
    public ForgeExchangeManager provideForgeExchangeManager() {
        ForgeExchangeManager fake = new ForgeExchangeManager() {
            @Override
            public long executeExchange(Exchange<ForgeExchangeResult> x) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ForgeExchangeResult rez = new ForgeExchangeResult(BasicResponseCodes.OK, "{\"session_ttl\":1440,\"session_info\":{\"user_id\":10,\"screen_name\":\"user10\"}}");
                        onTaskSuccess(1, rez);
                    }
                });
                t.start();

                return 1;
            }
        };


        return fake;
    }


    @Provides
    public ForgeAndroidTaskExecutor provideTaskExecutor() {
        return new ForgeAndroidTaskExecutor();
    }


    @Provides
    @Singleton
    @Named("forge result producer")
    public ResultProducer<ForgeExchangeResult> provideForgeResultProducer(ForgeHeaderResultProducer rp) {
        return rp;
    }


    @Provides
    @Singleton
    ForgeExchangeHelper provideForgeExchangeHelper(ForgeExchangeHelperImpl impl) {
        return impl;
    }


    @Provides
    @Named("base url")
    String provideBaseUrl() {
        return "https://fake.url";
    }
}
