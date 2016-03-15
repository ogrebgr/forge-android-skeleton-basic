package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;


import com.bolyartech.forge.android.app_unit.ActivityComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;


/**
 * Created by ogre on 2016-01-05 12:49
 */
abstract public class BaseActivity extends AppCompatActivity {
    private ActivityComponent mActivityComponent;

    abstract protected ActivityComponent createActivityComponent();


    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        mActivityComponent = createActivityComponent();
    }


    protected MyAppDaggerComponent getDependencyInjector() {
        return DependencyInjector.getInstance();
    }

}
