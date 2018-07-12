package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.android.app_unit.UnitManager;
import com.bolyartech.forge.android.app_unit.UnitManagerImpl;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.android.app_unit.rc_task.executor.ThreadRcTaskExecutor;
import com.bolyartech.forge.android.misc.RunOnUiThreadHelper;
import com.bolyartech.forge.android.misc.RunOnUiThreadHelperDefault;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ResLoginImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook.ResLoginFacebook;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook.ResLoginFacebookImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_google.ResLoginGoogle;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_google.ResLoginGoogleImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMain;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ResMainImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.rc_test.ResRcTest;
import com.bolyartech.forge.skeleton.dagger.basic.units.rc_test.ResRcTestImpl;
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
    RcTaskExecutor provideRcTaskExecutor(ThreadRcTaskExecutor impl) {
        return impl;
    }


    @Provides
    RunOnUiThreadHelper provideRunOnUiThreadHelper(RunOnUiThreadHelperDefault impl) {
        return impl;
    }


    @Provides
    @Singleton
    UnitManager provideUnitManager(UnitManagerImpl impl) {
        return impl;
    }


    @Provides
    ResMain providesResMain(ResMainImpl impl) {
        return impl;
    }


    @Provides
    ResLogin providesResLogin(ResLoginImpl impl) {
        return impl;
    }


    @Provides
    ResRegister providesResRegister(ResRegisterImpl impl) {
        return impl;
    }


    @Provides
    ResScreenName providesResScreenName(ResScreenNameImpl impl) {
        return impl;
    }


    @Provides
    ResSelectLogin providesResSelectLogin(ResSelectLoginImpl impl) {
        return impl;
    }

    @Provides
    ResLoginFacebook provideResLoginFacebook(ResLoginFacebookImpl impl) {
        return impl;
    }


    @Provides
    ResLoginGoogle provideResLoginGoogle(ResLoginGoogleImpl impl) {
        return impl;
    }


    @Provides
    ResRcTest provideResRcTest(ResRcTestImpl impl) {
        return impl;
    }
}
