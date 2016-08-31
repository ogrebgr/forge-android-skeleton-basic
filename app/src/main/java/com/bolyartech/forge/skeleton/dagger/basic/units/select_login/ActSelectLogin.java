package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
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
import javax.inject.Provider;


public class ActSelectLogin extends SessionActivity<ResSelectLogin> implements OperationResidentComponent.Listener,
        PerformsLogin {


    private static final int ACT_LOGIN = 1;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());
    private static final int RC_SIGN_IN = 9001;
    private static final int HIDE_COMM_WAIT_DIALOG_POSTPONE = 300;

    private CallbackManager mFacebookCallbackManager;


    private GoogleApiClient mGoogleApiClient;
    private SignInButton mGoogleSignInButton;


    @Inject
    Session mSession;

    @Inject
    Provider<ResSelectLoginImpl> mRes_SelectLoginImplProvider;

    private volatile boolean mInitialWaitDialogShown = false;
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


    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
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

        View view = getWindow().getDecorView();
        initViews(view);
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
                .requestIdToken(getString(R.string.google_client_id))
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
                getResident().checkFbLogin(token.getToken(), token.getUserId());
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
                Intent intent = new Intent(ActSelectLogin.this, ActLogin.class);
                startActivityForResult(intent, ACT_LOGIN);
            }
        });
    }


    @NonNull
    @Override
    public ResSelectLogin createResidentComponent() {
        return mRes_SelectLoginImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient == null) {
            initializaGoogleSignIn();
        }

        handleState(getResident().getOpState());
    }


    @Override
    public void onResidentOperationStateChanged() {
        handleState(getResident().getOpState());
    }


    private void handleState(OperationResidentComponent.OpState opState) {
        switch (opState) {
            case IDLE:
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case COMPLETED:
                if (getResident().isSuccess()) {
                    onLoginOk();
                } else {
                    onLoginFail();
                }
                getResident().completedStateAcknowledged();
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

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    getResident().checkGoogleLogin(acct.getIdToken());
                } else {
                    mLogger.error("Cannot get GoogleSignInAccount");
                }
            } else {
                getResident().completedStateAcknowledged();
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
