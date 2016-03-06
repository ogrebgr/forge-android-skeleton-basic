package com.bolyartech.forge.skeleton.dagger.basic.utils;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;


public class MyTestRunner extends AndroidJUnitRunner {
    @Override
    public Application newApplication(ClassLoader cl,
                                      String className,
                                      Context context) throws InstantiationException,
            IllegalAccessException,
            ClassNotFoundException {


        return super.newApplication(cl, MyTestApp.class.getName(), context);
    }
}
