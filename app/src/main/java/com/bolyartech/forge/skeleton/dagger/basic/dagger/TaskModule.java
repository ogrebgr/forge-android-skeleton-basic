package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.skeleton.dagger.basic.units.main.AutoregisterTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.AutoregisterTaskImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.LogoutTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.LogoutTaskImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.RegistrationTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.RegistrationTaskImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ScreenNameTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ScreenNameTaskImpl;

import dagger.Module;
import dagger.Provides;


@Module
public class TaskModule {
    @Provides
    LogoutTask provideLogoutTask(LogoutTaskImpl impl) {
        return impl;
    }


    @Provides
    AutoregisterTask provideAutoregisterTask(AutoregisterTaskImpl impl) {
        return impl;
    }


    @Provides
    RegistrationTask provideRegistrationTask(RegistrationTaskImpl impl) {
        return impl;
    }


    @Provides
    ScreenNameTask provideScreenNameTask(ScreenNameTaskImpl impl) {
        return impl;
    }
}
