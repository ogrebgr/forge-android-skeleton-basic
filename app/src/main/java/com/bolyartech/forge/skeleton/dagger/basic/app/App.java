package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;

import com.bolyartech.forge.android.app_unit.UnitApplication;
import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DefaultMyAppDaggerComponentHelper;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.misc.AcraKeyStoreFactory;
import com.squareup.leakcanary.LeakCanary;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.config.ACRAConfigurationException;
import org.acra.config.ConfigurationBuilder;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;


/**
 * Created by ogre on 2015-11-15 15:19
 */
@ReportsCrashes(formUri = "placeholder")
public class App extends UnitApplication {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());


    @Inject
    UnitManager mAppUnitManager;


    @Override
    public void onCreate() {
        initInjector();
        super.onCreate();
    }


    /**
     * This method will be called from onCreate() or from unit tests after the app is injected with its dependencies
     */
    public void onStart() {
        super.onStart();

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
            if (!getResources().getBoolean(R.bool.build_conf_disable_acra)) {
                initAcra();
            }
        }

        if (getResources().getBoolean(R.bool.build_conf_dev_mode)) {
//            enableStrictMode();
            LeakCanary.install(this);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    /**
     * Initializes the injector
     * Unit tests should use empty implementation of this method and return false in order to have a chance to
     * initialize the injector with test configuration
     *
     * @return true if dependency injector was initialized, false otherwise
     */
    protected boolean initInjector() {
        DependencyInjector.init(DefaultMyAppDaggerComponentHelper.create(this,
                getResources().getBoolean(R.bool.build_conf_dev_mode)));

        DependencyInjector.getInstance().inject(this);

        return true;
    }


    @Override
    protected void reset() {
        super.reset();

        mAppUnitManager = null;
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
        ConfigurationBuilder b = new ConfigurationBuilder(this);
        b.setKeyStoreFactoryClass(AcraKeyStoreFactory.class);
        b.setAdditionalSharedPreferences("forge", "login prefs");
        b.setFormUri(getString(R.string.build_conf_acra_url));
        b.setExcludeMatchingSharedPreferencesKeys("^Username.*", "^Password.*");
        b.setReportingInteractionMode(ReportingInteractionMode.SILENT);
        b.setAlsoReportToAndroidFramework(true);

        try {
            ACRA.init(this, b.build());
        } catch (ACRAConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
