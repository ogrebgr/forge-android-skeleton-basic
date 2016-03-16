package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bolyartech.forge.android.mvp.ActivityView;
import com.bolyartech.forge.android.mvp.MvpActivity;
import com.bolyartech.forge.android.mvp.Presenter;
import com.bolyartech.forge.android.app_unit.ActivityResultContainer;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;


/**
 * Created by ogre on 2016-01-05 12:49
 */
abstract public class MvpSessionActivity extends SessionActivity implements MvpActivity {
    private Presenter mPresenter;

    private ActivityResultContainer mActivityResultContainer;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityView activityView = createView();
        mPresenter = createPresenter(activityView);

        mPresenter.onCreate();
    }


    protected MyAppDaggerComponent getDependencyInjector() {
        return DependencyInjector.getInstance();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!isFinishing()) {
            mPresenter.onResume(mActivityResultContainer);
            mActivityResultContainer = null;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mActivityResultContainer = new ActivityResultContainer(requestCode, resultCode, data);
    }
}
