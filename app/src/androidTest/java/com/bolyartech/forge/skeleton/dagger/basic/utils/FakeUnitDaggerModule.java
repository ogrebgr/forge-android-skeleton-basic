package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppUnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLoginImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook.ResLoginFacebook;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook.ResLoginFacebookImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMain;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ResRegister;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ResRegisterImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ResScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ResScreenNameImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ResSelectLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ResSelectLoginImpl;

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
    protected ResMain providesResMain() {
        return mResMain;
    }


    @Provides
    @Singleton
    AppUnitManager provideMyAppUnitManagerForge(Session session) {
        return new AppUnitManager(session);
    }


    @Provides
    @Singleton
    UnitManager provideUnitManager(AppUnitManager my) {
        return my;
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

    @Provides
    protected ResLoginFacebook provideResLoginFacebook(ResLoginFacebookImpl impl) {
        return impl;
    }}
