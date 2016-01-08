package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.BaseActivity;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
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
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_SelectLogin extends SessionActivity implements DoesLogin {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    private CallbackManager mFacebookCallbackManager;


    private Res_SelectLogin mResident;


    @Inject
    Session mSession;

    @Inject
    Provider<Res_SelectLoginImpl> mRes_SelectLoginImplProvider;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyAppDialogs.showCommWaitDialog(getFragmentManager());
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                @Override
                public void onInitialized() {
                    initFacebookCallback();
                    MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                }
            });
        } else {
            initFacebookCallback();
        }

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
                mLogger.warn("Facebook native ERROR: {}", e);
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


//        ViewUtils.initButton(view, R.id.btn_facebook, new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!FacebookSdk.isInitialized()) {
//                    MyAppDialogs.showCommWaitDialog(getFragmentManager());
//                    FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
//                        @Override
//                        public void onInitialized() {
//                            MyAppDialogs.hideCommWaitDialog(getFragmentManager());
//                        }
//                    });
//                }
//            }
//        });
    }


    @Override
    public ResidentComponent createResidentComponent() {
        return mRes_SelectLoginImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        mResident = (Res_SelectLogin) getResidentComponent();

        handleState(mResident.getState());
    }

    @Subscribe
    public void onEv_StateChanged(Ev_StateChanged ev) {
        handleState(mResident.getState());
    }


    private void handleState(Res_SelectLogin.State state) {
        switch (state) {
            case IDLE:
                break;
            case WAITING_FB_CHECK:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case FB_CHECK_OK:
                onFbLoginOk();
                break;
            case FB_CHECK_FAIL:
                onFbLoginFail();
                break;
        }
    }

    private void onFbLoginOk() {
        mResident.resetState();
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        setResult(Activity.RESULT_OK);
        finish();
    }


    private void onFbLoginFail() {
        mResident.resetState();
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        MyAppDialogs.showCommProblemDialog(getFragmentManager());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == RC_SIGN_IN) {
//            mLogger.debug("onActivityResult google");
//            // If the error resolution was not successful we should not resolve further.
//            if (resultCode != RESULT_OK) {
//                mShouldResolve = false;
//            }
//
//            mIsResolving = false;
//            mGoogleApiClient.connect();
//        } else
        if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mLogger.debug("onActivityResult facebook");
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }
}
