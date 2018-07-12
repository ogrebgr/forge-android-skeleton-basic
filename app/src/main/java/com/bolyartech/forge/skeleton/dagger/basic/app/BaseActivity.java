package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.support.v7.app.AppCompatActivity;

import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;


/**
 * Created by ogre on 2016-01-05 12:49
 */
abstract public class BaseActivity extends AppCompatActivity {
    protected MyAppDaggerComponent getDependencyInjector() {
        return DependencyInjector.getInstance();
    }

}
