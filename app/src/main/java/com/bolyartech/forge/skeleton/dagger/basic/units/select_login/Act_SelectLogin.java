package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.BaseActivity;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Act_SelectLogin extends SessionActivity implements DoesLogin {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    private CallbackManager mFacebookCallbackManager;


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

        initFacebookCallback();
    }


    private void initFacebookCallback() {
        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mLogger.debug("Facebook native OK");
                AccessToken token = loginResult.getAccessToken();

                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                mResident.checkFbLogin(token.getToken(), token.getUserId());
            }


            @Override
            public void onCancel() {
                mLogger.debug("Facebook native cancelled");
            }


            @Override
            public void onError(FacebookException e) {
                mLogger.debug("Facebook native ERROR");
                MyAppDialogs.showFbLoginErrorDialog(getFragmentManager());
            }
        });
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
                if (!FacebookSdk.isInitialized()) {
                    MyAppDialogs.showCommWaitDialog(getFragmentManager());
                    FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                        @Override
                        public void onInitialized() {
                            MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                        }
                    });
                }
            }
        });
    }


    @Override
    public ResidentComponent createResidentComponent() {
        return null;
    }
}
