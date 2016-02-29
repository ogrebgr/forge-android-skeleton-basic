package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class UnitManagerDaggerModule {
    private final MyAppUnitManager mMyAppUnitManager;


    public UnitManagerDaggerModule(MyAppUnitManager myAppUnitManager) {
        mMyAppUnitManager = myAppUnitManager;
    }


    @Provides
    @Singleton
    UnitManager provideUnitManager() {
        return mMyAppUnitManager;
    }
}
