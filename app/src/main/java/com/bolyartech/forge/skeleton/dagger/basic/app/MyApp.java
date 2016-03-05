package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;

import com.bolyartech.forge.android.app_unit.UnitApplication;
import com.bolyartech.forge.base.misc.ForUnitTestsOnly;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.base.task.ForgeTaskExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.AppInfoDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DaggerMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.ExchangeDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitManagerDaggerModule;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;
import org.slf4j.LoggerFactory;

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


/**
 * Created by ogre on 2015-11-15 15:19
 */
@ReportsCrashes(formUri = "placeholder")
public class MyApp extends UnitApplication {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    private MyAppDaggerComponent mDependencyInjector;


    @Override
    public void onCreate() {
        super.onCreate();

        mLogger.debug("presni");

        initInjector();

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            enableStrictMode();
        }

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            initAcra(false);
        }
    }


    protected void initInjector() {
        HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(createOkHttpClient());

        MyAppUnitManager myAppUnitManager = new MyAppUnitManager();

        mDependencyInjector = DaggerMyAppDaggerComponent.builder().
                myAppDaggerModule(createMyAppDaggerModule()).
                appInfoDaggerModule(createAppInfoDaggerModule()).
                exchangeDaggerModule(createExchangeDaggerModule(myAppUnitManager)).
                httpsDaggerModule(httpsDaggerModule).
                unitManagerDaggerModule(new UnitManagerDaggerModule(myAppUnitManager)).
                build();

        mDependencyInjector.inject(this);

        onInjectorInitialized();
    }


    protected void onInjectorInitialized() {
    }


    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder b = new OkHttpClient.Builder();


        try {
            KeyStore keyStore = createKeystore();

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            SSLContext sslContext = SSLContext.getInstance("SSL");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            keyManagerFactory.init(keyStore, getString(R.string.bks_keystore_password).toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
            b.sslSocketFactory(sslContext.getSocketFactory());
        } catch (KeyStoreException | KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        return b.build();
    }


    private KeyStore createKeystore() {
        InputStream is = getResources().openRawResource(R.raw.forge_skeleton);
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("BKS");
            ks.load(is, getString(R.string.bks_keystore_password).toCharArray());
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


    private ExchangeDaggerModule createExchangeDaggerModule(MyAppUnitManager myAppUnitManager) {
        ForgeExchangeManager fem = new ForgeExchangeManager(new ForgeTaskExecutor());

        fem.addListener(myAppUnitManager);
        fem.start();

        return new ExchangeDaggerModule(getString(R.string.build_conf_base_url),
                fem);
    }


    private MyAppDaggerModule createMyAppDaggerModule() {
        return new MyAppDaggerModule(this);
    }


    private AppInfoDaggerModule createAppInfoDaggerModule() {
        PackageInfo pInfo;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            if (pInfo == null) {
                throw new NullPointerException("pInfo is null");
            }
        } catch (PackageManager.NameNotFoundException e1) {
            throw new IllegalStateException(e1);
        }

        return new AppInfoDaggerModule(getString(R.string.app_key),
                String.valueOf(pInfo.versionCode));
    }


    public MyAppDaggerComponent getDependencyInjector() {
        return mDependencyInjector;
    }


    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                //                .detectDiskReads()
                //                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyDeath()
                .build());

        StrictMode.VmPolicy.Builder b = new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath();
        StrictMode.setVmPolicy(b.build());
    }


    private void initAcra(boolean disableLogcatCollection) {

        ACRAConfiguration conf = ACRA.getNewDefaultConfig(this);
        conf.setFormUri(getString(R.string.build_conf_acra_url));
        conf.setAdditionalSharedPreferences(new String[]{"glasuvalnik"});
        conf.setAdditionalSharedPreferences(new String[]{"login prefs"});
        conf.setExcludeMatchingSharedPreferencesKeys(new String[]{"^Username.*",
                "^Password.*"});

        KeyStore ks;
        try {
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(getResources().openRawResource(R.raw.forge_skeleton), getString(R.string.bks_keystore_password).toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new AssertionError("Cannot initialize SSL cert for ACRA");
        }

        conf.setKeyStore(ks);
        ACRA.init(this, conf);
    }


    @ForUnitTestsOnly
    public void setDependencyInjector(MyAppDaggerComponent dependencyInjector) {
        mDependencyInjector = dependencyInjector;
    }
}
