package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;


@Module
public class HttpsDaggerModule {
    private final OkHttpClient mOkHttpClient;


    public HttpsDaggerModule(OkHttpClient okHttpClient) {
        super();
        mOkHttpClient = okHttpClient;
    }


    @Provides
    @Singleton
    OkHttpClient provideOkHttpClient() {
        return mOkHttpClient;
    }
}


   
