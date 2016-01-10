package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;

import com.bolyartech.forge.app_unit.UnitApplication;
import com.bolyartech.forge.exchange.ForgeExchangeFunctionality;
import com.bolyartech.forge.exchange.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.AppInfoDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DaggerMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.ExchangeDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerModule;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.inject.Inject;


/**
 * Created by ogre on 2015-11-15 15:19
 */
@ReportsCrashes(formUri = "placeholder")
public class MyApp extends UnitApplication {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    private MyAppDaggerComponent mDependencyInjector;

    @Inject
    ForgeExchangeFunctionality mExchangeFunctionality;

    @Inject
    ForgeExchangeManager mForgeExchangeManager;


    @Override
    public void onCreate() {
        super.onCreate();

        initInjector();
        getDependencyInjector().inject(this);

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            enableStrictMode();
        }

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            initAcra(false);
        }

        mExchangeFunctionality.start();
        mExchangeFunctionality.addListener(mForgeExchangeManager);
    }


    public void shutdown() {
        mExchangeFunctionality.removeListener(mForgeExchangeManager);
        mExchangeFunctionality.shutdown();
        mExchangeFunctionality = null;
        mForgeExchangeManager = null;
    }


    private void initInjector() {
        InputStream keyStore = getResources().openRawResource(R.raw.forge_skeleton);

        HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(keyStore,
                getString(R.string.bks_keystore_password),
                80,
                getResources().getInteger(R.integer.build_conf_https_port));


        mDependencyInjector = DaggerMyAppDaggerComponent.builder().
                myAppDaggerModule(createMyAppDaggerModule()).
                appInfoDaggerModule(createAppInfoDaggerModule()).
                exchangeDaggerModule(createExchangeDaggerModule()).
                httpsDaggerModule(httpsDaggerModule).
                build();
    }


    private ExchangeDaggerModule createExchangeDaggerModule() {
        return new ExchangeDaggerModule(getString(R.string.build_conf_base_url));
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

}
