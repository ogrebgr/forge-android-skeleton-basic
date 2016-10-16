package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMain;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FakeUnitDaggerModule {
    private ResMain mResMain;


    public void setResMain(ResMain resMain) {
        mResMain = resMain;
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

    @Provides
    protected ResMain providesResMain() {
        return mResMain;
    }
}
