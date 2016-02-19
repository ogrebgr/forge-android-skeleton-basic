package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.http.ForgeCloseableHttpClient;
import com.bolyartech.forge.http.functionality.HttpFunctionality;
import com.bolyartech.forge.http.functionality.HttpFunctionalityWCookiesImpl;
import com.bolyartech.forge.http.misc.SynchronizedCookieStore;
import com.bolyartech.forge.http.ssl.DefaultSslHttpClient;

import java.security.KeyStore;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import forge.apache.http.client.CookieStore;
import forge.apache.http.impl.client.BasicCookieStore;


@Module
public class HttpsDaggerModule {
    private static final int DEFALT_PORT_HTTP = 80;
    private static final int DEFALT_PORT_HTTPS = 443;

    private final KeyStore mKeyStore;
    @SuppressWarnings("FieldCanBeLocal")
    private int mHttpPort = DEFALT_PORT_HTTP;
    @SuppressWarnings("FieldCanBeLocal")
    private int mHttpsPort = DEFALT_PORT_HTTPS;


    public HttpsDaggerModule(KeyStore keyStore) {
        super();
        mKeyStore = keyStore;
    }


    public HttpsDaggerModule(KeyStore keyStore, int httpPort, int httpsPort) {
        this(keyStore);
        mHttpPort = httpPort;
        mHttpsPort = httpsPort;
    }


    @Provides
    @Singleton
    ForgeCloseableHttpClient providesHttpClient() {
        return new DefaultSslHttpClient(mKeyStore);
    }


    @Provides
    @Singleton
    CookieStore providesCookieStore() {
        return new SynchronizedCookieStore(new BasicCookieStore());
    }


    @Provides
    @Singleton
    HttpFunctionality providesHttpFunctionalityWCookies(ForgeCloseableHttpClient httpClient,
                                                                CookieStore store
    ) {
        return new HttpFunctionalityWCookiesImpl(httpClient, store);
    }
}


   
