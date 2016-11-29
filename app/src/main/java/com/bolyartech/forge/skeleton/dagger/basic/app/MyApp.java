package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import com.bolyartech.forge.android.app_unit.UnitApplication;
import com.bolyartech.forge.android.task.ForgeAndroidTaskExecutor;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.misc.ForUnitTestsOnly;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DefaultMyAppDaggerComponentHelper;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.squareup.leakcanary.LeakCanary;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-15 15:19
 */
@ReportsCrashes(formUri = "placeholder")
public class MyApp extends UnitApplication {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    @Inject
    ForgeAndroidTaskExecutor mForgeAndroidTaskExecutor;

    @Inject
    MyAppUnitManager mMyAppUnitManager;

    @Inject
    ForgeExchangeManager mForgeExchangeManager;

    @Inject
    Provider<ForgeAndroidTaskExecutor> mForgeAndroidTaskExecutorProvider;


    @Override
    public void onCreate() {
        initInjector();
        super.onCreate();
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    /**
     * This method will be called from onCreate() or from unit tests after the app is injected with its dependencies
     */
    public void onStart() {
        super.onStart();

        mLogger.debug("mForgeExchangeManager {}", mForgeExchangeManager);

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            if (!getResources().getBoolean(R.bool.build_conf_disable_acra)) {
                initAcra();
            }
        }

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            enableStrictMode();
            LeakCanary.install(this);
        }

        mForgeExchangeManager.addListener(mMyAppUnitManager);
        mForgeExchangeManager.start(mForgeAndroidTaskExecutorProvider.get());
    }


    @Override
    protected void onInterfaceResumed() {
        super.onInterfaceResumed();

        if (!mForgeExchangeManager.isStarted()) {
            mForgeExchangeManager.addListener(mMyAppUnitManager);
            mForgeExchangeManager.start(mForgeAndroidTaskExecutorProvider.get());
        }
    }


    @Override
    protected void onInterfacePaused() {
        super.onInterfacePaused();

        mForgeExchangeManager.removeListener(mMyAppUnitManager);
        mForgeExchangeManager.shutdown();
    }


    /**
     * Initializes the injector
     * Unit tests should use empty implementation of this method and return false in order to have a chance to
     * initialize the injector with test configuration
     * @return true if dependency injector was initialized, false otherwise
     */
    protected boolean initInjector() {
        DependencyInjector.init(DefaultMyAppDaggerComponentHelper.create(this,
                getResources().getBoolean(R.bool.build_conf_dev_mode)));

        DependencyInjector.getInstance().inject(this);

        return true;
    }


    private void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
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


    private void initAcra() {
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
    public ForgeAndroidTaskExecutor getForgeAndroidTaskExecutor() {
        return mForgeAndroidTaskExecutor;
    }


    @Override
    protected void reset() {
        super.reset();

        if (mForgeAndroidTaskExecutor != null) {
            mForgeAndroidTaskExecutor.shutdown();
        }
        mForgeAndroidTaskExecutor = null;
        mMyAppUnitManager = null;
        mForgeExchangeManager = null;
        mForgeAndroidTaskExecutorProvider = null;
    }
}
