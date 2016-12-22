package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLoginImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMain;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMainImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ResRegister;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ResRegisterImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ResScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ResScreenNameImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ResSelectLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ResSelectLoginImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class UnitDaggerModule {

    public UnitDaggerModule() {
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
    protected ResMain providesResMain(ResMainImpl impl) {
        return impl;
    }


    @Provides
    protected ResLogin providesResLogin(ResLoginImpl impl) {
        return impl;
    }


    @Provides
    protected ResRegister providesResRegister(ResRegisterImpl impl) {
        return impl;
    }


    @Provides
    protected ResScreenName providesResScreenName(ResScreenNameImpl impl) {
        return impl;
    }


    @Provides
    protected ResSelectLogin providesResSelectLogin(ResSelectLoginImpl impl) {
        return impl;
    }
}
