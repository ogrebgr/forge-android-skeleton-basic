package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
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
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_SelectLogin extends SessionActivity implements DoesLogin {
    private static final int ACT_LOGIN = 1;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());
    private static final int RC_SIGN_IN = 9001;
    private static final int HIDE_COMM_WAIT_DIALOG_POSTPONE = 300;

    private CallbackManager mFacebookCallbackManager;


    private Res_SelectLogin mResident;

    private GoogleApiClient mGoogleApiClient;
    private SignInButton mGoogleSignInButton;


    @Inject
    Session mSession;

    @Inject
    Provider<Res_SelectLoginImpl> mRes_SelectLoginImplProvider;

    private volatile boolean  mInitialWaitDialogShown = false;
    private int mWaitingInitializations = 0;

    private final Handler mHandler = new Handler();

    private Runnable mInitialWaitDialogDismisser = new Runnable() {
        @Override
        public void run() {
            if (mInitialWaitDialogShown) {
                if (MyAppDialogs.hideCommWaitDialog(getFragmentManager())) {
                    mInitialWaitDialogShown = false;
                } else {
                    mHandler.postDelayed(mInitialWaitDialogDismisser, HIDE_COMM_WAIT_DIALOG_POSTPONE);
                }
            }
        }
    };


    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks  = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {
            mGoogleSignInButton.setEnabled(true);
            initializationCompleted();
        }


        @Override
        public void onConnectionSuspended(int i) {
            // empty
        }
    };

    private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            mGoogleApiClient = null;
            mGoogleSignInButton.setEnabled(false);
            initializationCompleted();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeFacebookSdk();

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


    private void initializeFacebookSdk() {
        if (!FacebookSdk.isInitialized()) {
            waitForInitialization();

            FacebookSdk.sdkInitialize(getApplicationContext(), new FacebookSdk.InitializeCallback() {
                @Override
                public void onInitialized() {
                    initFacebookCallback();
                    initializationCompleted();
                }
            });
        } else {
            initFacebookCallback();
        }
    }


    private void initializaGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInButton = (SignInButton) findViewById(R.id.btn_google_login);
        mGoogleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mGoogleSignInButton.setScopes(gso.getScopeArray());

        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(mGoogleConnectionCallbacks)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .build();
    }




    private void waitForInitialization() {
        mLogger.debug("waitForInitialization");
        if (mWaitingInitializations == 0) {
            MyAppDialogs.showCommWaitDialog(getFragmentManager());
        }
        mInitialWaitDialogShown = true;
        mWaitingInitializations++;
    }


    private void initializationCompleted() {
        mLogger.debug("initializationCompleted");
        mWaitingInitializations--;
        if (mWaitingInitializations == 0) {
            hideInitialWaitDialog();
        }
    }


    /**
     * Sometimes initialization of facebook sdk and google api client is too fast and hideCommWaitDialog() is called
     * before Android had a chance to process the que and show the wait dialog so hideCommWaitDialog() cannot find it by id
     * and close it.
     */
    private synchronized void hideInitialWaitDialog() {
        if (MyAppDialogs.hideCommWaitDialog(getFragmentManager())) {
            mInitialWaitDialogShown = false;
        } else {
            mHandler.postDelayed(mInitialWaitDialogDismisser, HIDE_COMM_WAIT_DIALOG_POSTPONE);
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
                startActivityForResult(intent, ACT_LOGIN);
            }
        });
    }


    @Override
    public ResidentComponent createResidentComponent() {
        return mRes_SelectLoginImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            initializaGoogleSignIn();
        }

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

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            GoogleSignInAccount acct = result.getSignInAccount();
            if (acct != null) {
                mResident.checkGoogleLogin(acct.getIdToken());
            } else {
                mLogger.error("Cannot get GoogleSignInAccount");
            }
        } else if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
            mLogger.debug("onActivityResult facebook");
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        } else if (requestCode == ACT_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }


}
