package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.app_unit.UnitManager;
import com.bolyartech.forge.app_unit.UnitManagerImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class UnitManagerDaggerModule {
    @Provides
    @Singleton
    UnitManager provideUnitManager(UnitManagerImpl cm) {
        return cm;
    }
}
