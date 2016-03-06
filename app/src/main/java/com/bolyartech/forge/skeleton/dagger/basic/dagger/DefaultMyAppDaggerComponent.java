package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.bolyartech.forge.android.task.ForgeAndroidTaskExecutor;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyApp;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;


public class DefaultMyAppDaggerComponent {
    public static MyAppDaggerComponent  create(MyApp app) {
        HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(createOkHttpClient(app));

        MyAppUnitManager myAppUnitManager = new MyAppUnitManager();

        return DaggerMyAppDaggerComponent.builder().
                myAppDaggerModule(createMyAppDaggerModule(app)).
                appInfoDaggerModule(createAppInfoDaggerModule(app)).
                exchangeDaggerModule(createExchangeDaggerModule(myAppUnitManager, app)).
                httpsDaggerModule(httpsDaggerModule).
                unitManagerDaggerModule(new UnitManagerDaggerModule(myAppUnitManager)).
                build();

    }


    public static OkHttpClient createOkHttpClient(MyApp app) {
        OkHttpClient.Builder b = new OkHttpClient.Builder();


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


        return b.build();
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


    public static ExchangeDaggerModule createExchangeDaggerModule(MyAppUnitManager myAppUnitManager, MyApp app) {
        ForgeAndroidTaskExecutor te = new ForgeAndroidTaskExecutor();
        ForgeExchangeManager fem = new ForgeExchangeManager(te);

        fem.addListener(myAppUnitManager);
        fem.start();

        return new ExchangeDaggerModule(app.getString(R.string.build_conf_base_url),
                fem,
                te);
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