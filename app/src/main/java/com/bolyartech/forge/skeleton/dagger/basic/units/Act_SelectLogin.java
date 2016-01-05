package com.bolyartech.forge.skeleton.dagger.basic.units;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.BaseActivity;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Act_SelectLogin extends BaseActivity {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    @Inject
    Session mSession;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act__select_login);

        getDependencyInjector().inject(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (mSession.isLoggedIn()) {
            mLogger.debug("Still logged in. User must logout first. Finishing activity.");
            finish();
        } else {
            View view = getWindow().getDecorView();
            initViews(view);
        }
    }


    private void initViews(View view) {
        ViewUtils.initButton(view, R.id.btn_manual, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Act_SelectLogin.this, Act_Login.class);
                startActivity(intent);
                finish();
            }
        });

        ViewUtils.initButton(view, R.id.btn_google, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        ViewUtils.initButton(view, R.id.btn_facebook, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
