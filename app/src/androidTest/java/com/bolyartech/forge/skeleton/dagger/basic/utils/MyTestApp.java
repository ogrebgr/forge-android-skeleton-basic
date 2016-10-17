package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.skeleton.dagger.basic.app.MyApp;


public class MyTestApp extends MyApp {


    @Override
    protected boolean initInjector() {
        useManualOnStartCall();
        return false;
    }


    @Override
    public void reset() {
        super.reset();
    }
}
