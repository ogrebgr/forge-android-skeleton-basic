package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionImpl;

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
    Session provideIzgSession(SessionImpl sess) {
        return sess;
    }
}
