package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.http.ForgeCloseableHttpClient;
import com.bolyartech.forge.http.functionality.HttpFunctionalityWCookies;
import com.bolyartech.forge.http.functionality.HttpFunctionalityWCookiesImpl;
import com.bolyartech.forge.http.misc.SynchronizedCookieStore;
import com.bolyartech.forge.http.ssl.DefaultSslHttpClient;

import java.io.InputStream;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import forge.apache.http.client.CookieStore;
import forge.apache.http.impl.client.BasicCookieStore;


@Module
public class HttpsDaggerModule {
    private static final int DEFALT_PORT_HTTP = 80;
    private static final int DEFALT_PORT_HTTPS = 443;

    private final InputStream mKeyStore;
    private final String mKeyStorePassword;
    @SuppressWarnings("FieldCanBeLocal")
    private int mHttpPort = DEFALT_PORT_HTTP;
    @SuppressWarnings("FieldCanBeLocal")
    private int mHttpsPort = DEFALT_PORT_HTTPS;


    public HttpsDaggerModule(InputStream keyStore, String keyStorePassword) {
        super();
        mKeyStore = keyStore;
        mKeyStorePassword = keyStorePassword;
    }


    public HttpsDaggerModule(InputStream keyStore, String keyStorePassword, int httpPort, int httpsPort) {
        this(keyStore, keyStorePassword);
        mHttpPort = httpPort;
        mHttpsPort = httpsPort;
    }


    @Provides
    @Singleton
    ForgeCloseableHttpClient providesHttpClient() {
        return new DefaultSslHttpClient(mKeyStore, mKeyStorePassword);
    }


    @Provides
    @Singleton
    CookieStore providesCookieStore() {
        return new SynchronizedCookieStore(new BasicCookieStore());
    }


    @Provides
    @Singleton
    HttpFunctionalityWCookies providesHttpFunctionalityWCookies(ForgeCloseableHttpClient httpClient,
                                                                CookieStore store
    ) {
        return new HttpFunctionalityWCookiesImpl(httpClient, store);
    }
}


   
