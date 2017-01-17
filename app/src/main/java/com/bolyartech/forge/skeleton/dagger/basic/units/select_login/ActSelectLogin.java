package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.ActivityResult;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ActLogin;
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

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActSelectLogin extends SessionActivity<ResSelectLogin> implements PerformsLogin,
        OperationResidentComponent.Listener {


    private static final int ACT_LOGIN = 1;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());
    private static final int GOOGLE_SIGN_IN = 9001;

    private CallbackManager mFacebookCallbackManager;


    private GoogleApiClient mGoogleApiClient;
    private SignInButton mGoogleSignInButton;
    private AccessToken mAccessToken;

    private ActivityResult mActivityResult;

    @Inject
    Session mSession;

    @Inject
    Lazy<ResSelectLogin> mRes_SelectLoginLazy;

    private volatile boolean mInitialWaitDialogShown = false;
    private int mWaitingInitializations = 0;


    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
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

    private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            mGoogleApiClient = null;
            mGoogleSignInButton.setEnabled(false);
            initializationCompleted();
        }
    };


    @Override
    public void onResidentOperationStateChanged() {
        handleState();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        if (getResources().getBoolean(R.bool.facebook_login_enabled)) {
            initializeFacebookSdk();
        }

        setContentView(R.layout.act__select_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View view = getWindow().getDecorView();
        initViews(view);
    }


    private void initializeFacebookSdk() {
        if (!FacebookSdk.isInitialized()) {
            waitForInitialization();

            FacebookSdk.sdkInitialize(getApplicationContext(), () -> {
                initFacebookCallback();
                initializationCompleted();
            });
        } else {
            initFacebookCallback();
        }
    }


    private void initializeGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();

        mGoogleSignInButton = (SignInButton) findViewById(R.id.btn_google_login);
        mGoogleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mGoogleSignInButton.setScopes(gso.getScopeArray());

        mGoogleSignInButton.setOnClickListener(v -> {
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(mGoogleConnectionCallbacks)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .build();

//        mGoogleApiClient.disconnect();
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
            MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        }
    }


    private void initFacebookCallback() {
        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mLogger.debug("Facebook native OK");

                mAccessToken = loginResult.getAccessToken();
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
        ViewUtils.initButton(view, R.id.btn_manual, v -> {
            Intent intent = new Intent(ActSelectLogin.this, ActLogin.class);
            startActivityForResult(intent, ACT_LOGIN);
        });
    }


    @NonNull
    @Override
    public ResSelectLogin createResidentComponent() {
        return mRes_SelectLoginLazy.get();
    }


    @Override
    public void onResume() {
        super.onResume();
        mLogger.debug("onResume");

        if (mActivityResult != null) {
            handleActivityResult();
            mActivityResult = null;
        }

        if (getResources().getBoolean(R.bool.google_login_enabled)) {
            if (mGoogleApiClient == null) {
                initializeGoogleSignIn();
            }
        }

        handleState();
    }


    private void handleActivityResult() {
        if (getResources().getBoolean(R.bool.google_login_enabled)) {
            if (mActivityResult.getRequestCode() == GOOGLE_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(mActivityResult.getData());

                if (result.isSuccess()) {
                    GoogleSignInAccount acct = result.getSignInAccount();
                    if (acct != null) {
                        getRes().checkGoogleLogin(acct.getIdToken());
                    } else {
                        mLogger.error("Cannot get GoogleSignInAccount");
                    }
                } else {

                    MyAppDialogs.showInvalidLoginDialog(getFragmentManager());
                }
            }
        }


        if (getResources().getBoolean(R.bool.facebook_login_enabled)) {
            if (mActivityResult.getRequestCode() == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                mLogger.debug("onActivityResult facebook");
                mFacebookCallbackManager.onActivityResult(mActivityResult.getRequestCode(),
                        mActivityResult.getResultCode(),
                        mActivityResult.getData());
            }
        }


        if (mActivityResult.getRequestCode() == ACT_LOGIN) {
            if (mActivityResult.getResultCode() == Activity.RESULT_OK) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mLogger.debug("onPause");
    }


    private void handleState() {
        OperationResidentComponent.OpState opState = getRes().getOpState();
        mLogger.debug("State: " + opState);
        switch (opState) {
            case IDLE:
                if (mAccessToken != null) {
                    MyAppDialogs.showCommWaitDialog(getFragmentManager());
                    AccessToken tmp = mAccessToken;
                    mAccessToken = null;
                    getRes().checkFbLogin(tmp.getToken());
                }
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case ENDED:
                if (getRes().isSuccess()) {
                    onLoginOk();
                } else {
                    onLoginFail();
                }
                getRes().endedStateAcknowledged();
                break;
        }
    }


    private void onLoginOk() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        setResult(Activity.RESULT_OK);
        finish();
    }


    private void onLoginFail() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        MyAppDialogs.showCommProblemDialog(getFragmentManager());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mActivityResult = new ActivityResult(requestCode, resultCode, data);
    }
}
