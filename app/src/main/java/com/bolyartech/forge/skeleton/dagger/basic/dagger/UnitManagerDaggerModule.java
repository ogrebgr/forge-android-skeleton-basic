package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManagerForge;
import com.bolyartech.forge.base.session.Session;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class UnitManagerDaggerModule {

    public UnitManagerDaggerModule() {
    }


    @Provides
    @Singleton
    MyAppUnitManagerForge provideMyAppUnitManagerForge(Session session) {
        return new MyAppUnitManagerForge(session);
    }

    @Provides
    @Singleton
    UnitManager provideUnitManager(MyAppUnitManagerForge my) {
        return my;
    }
}
