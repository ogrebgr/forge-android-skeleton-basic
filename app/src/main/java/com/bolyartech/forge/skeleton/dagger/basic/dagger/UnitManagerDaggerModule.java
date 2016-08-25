package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;
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
    MyAppUnitManager provideMyAppUnitManagerForge(Session session) {
        return new MyAppUnitManager(session);
    }

    @Provides
    @Singleton
    UnitManager provideUnitManager(MyAppUnitManager my) {
        return my;
    }
}
