package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.base.session.Session;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class FakeSessionDaggerModule {
    private final Session mSession;


    public FakeSessionDaggerModule(Session session) {
        mSession = session;
    }


    @Provides
    @Singleton
    Session provideSession() {
        return mSession;
    }
}
