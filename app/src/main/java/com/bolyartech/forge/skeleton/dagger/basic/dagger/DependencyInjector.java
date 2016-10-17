package com.bolyartech.forge.skeleton.dagger.basic.dagger;

import com.bolyartech.forge.base.misc.ForUnitTestsOnly;


public class DependencyInjector {
    private static MyAppDaggerComponent mDependencyInjector;

    private DependencyInjector() {
        throw new AssertionError("No instances allowed");
    }


    public static void init(MyAppDaggerComponent di) {
        if (mDependencyInjector == null) {
            mDependencyInjector = di;
        } else {
            throw new IllegalStateException("Already initialized");
        }
    }


    public static MyAppDaggerComponent getInstance() {
        if (mDependencyInjector == null) {
            throw new IllegalStateException("Not initialized. You must call init().");
        }
        return mDependencyInjector;
    }


    @ForUnitTestsOnly
    public static void reset() {
        mDependencyInjector = null;
    }

}
