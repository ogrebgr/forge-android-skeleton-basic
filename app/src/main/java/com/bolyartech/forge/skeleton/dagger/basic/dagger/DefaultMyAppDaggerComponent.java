package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyApp;
import com.bolyartech.forge.base.misc.LoggingInterceptor;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.CookiePersistor;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Cookie;
import okhttp3.OkHttpClient;


public class DefaultMyAppDaggerComponent {
    private DefaultMyAppDaggerComponent() {
        throw new AssertionError("No instances allowed");
    }


    public static MyAppDaggerComponent create(MyApp app, boolean debug) {
        HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(createOkHttpClient(app, debug));


        return DaggerMyAppDaggerComponent.builder().
                myAppDaggerModule(createMyAppDaggerModule(app)).
                appInfoDaggerModule(createAppInfoDaggerModule(app)).
                exchangeDaggerModule(createExchangeDaggerModule(app)).
                httpsDaggerModule(httpsDaggerModule).
                build();

    }


    public static OkHttpClient createOkHttpClient(MyApp app, boolean debug) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        if (debug) {
            b.addInterceptor(new LoggingInterceptor());
            TrustManager tm = createDummyTrustManager();

            try {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, new TrustManager[]{tm}, new java.security.SecureRandom());

                final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                b.sslSocketFactory(sslSocketFactory);

                b.hostnameVerifier(new HostnameVerifier() {
                    @SuppressLint("BadHostnameVerifier")
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            } catch (KeyManagementException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {

            try {
                KeyStore keyStore = createKeystore(app);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                keyManagerFactory.init(keyStore, app.getString(R.string.bks_keystore_password).toCharArray());
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
                b.sslSocketFactory(sslContext.getSocketFactory());
            } catch (KeyStoreException | KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }

        ClearableCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), createFakePersistor());
        b.cookieJar(cookieJar);

        return b.build();
    }


    private static CookiePersistor createFakePersistor() {
        return new CookiePersistor() {
            @Override
            public List<Cookie> loadAll() {
                return new ArrayList<>();
            }


            @Override
            public void saveAll(Collection<Cookie> collection) {
            }


            @Override
            public void removeAll(Collection<Cookie> collection) {
            }


            @Override
            public void clear() {
            }
        };
    }


    private static TrustManager createDummyTrustManager() {
        return new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }


            @SuppressLint("TrustAllX509TrustManager")
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }


            @SuppressLint("TrustAllX509TrustManager")
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        };
    }


    public static KeyStore createKeystore(MyApp app) {
        InputStream is = app.getResources().openRawResource(R.raw.forge_skeleton);
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("BKS");
            ks.load(is, app.getString(R.string.bks_keystore_password).toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new IllegalStateException("Cannot create the keystore");
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.out.print(e.getMessage());
            }
        }

        return ks;
    }


    public static ExchangeDaggerModule createExchangeDaggerModule(MyApp app) {
        return new ExchangeDaggerModule(app.getString(R.string.build_conf_base_url));
    }


    public static MyAppDaggerModule createMyAppDaggerModule(MyApp app) {
        return new MyAppDaggerModule(app);
    }


    public static AppInfoDaggerModule createAppInfoDaggerModule(MyApp app) {
        PackageInfo pInfo;
        try {
            pInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
            if (pInfo == null) {
                throw new NullPointerException("pInfo is null");
            }
        } catch (PackageManager.NameNotFoundException e1) {
            throw new IllegalStateException(e1);
        }

        return new AppInfoDaggerModule(app.getString(R.string.app_key),
                String.valueOf(pInfo.versionCode));
    }


}
