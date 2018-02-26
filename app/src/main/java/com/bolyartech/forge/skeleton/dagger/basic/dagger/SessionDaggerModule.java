package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.base.session.SessionImpl;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutorImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


/**
 * Created by ogre on 2015-07-15
 */
@Module
public class SessionDaggerModule {
    @Provides
    @Singleton
    Session provideSession(SessionImpl session) {
        return session;
    }


    @Provides
    @Singleton
    SessionForgeExchangeExecutor provideAppForgeExchangeExecutor(SessionForgeExchangeExecutorImpl impl) {
        return impl;
    }
}
