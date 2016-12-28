package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.skeleton.dagger.basic.app.App;


public class TestApp extends App {


    @Override
    public void reset() {
        super.reset();
    }


    @Override
    protected boolean initInjector() {
        useManualOnStartCall();
        return false;
    }
}
