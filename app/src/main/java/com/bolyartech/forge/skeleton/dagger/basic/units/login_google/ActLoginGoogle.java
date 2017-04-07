package com.bolyartech.forge.skeleton.dagger.basic.units.login_google;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.ActivityResult;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.OpSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActLoginGoogle extends OpSessionActivity<ResLoginGoogle> implements PerformsLogin,
        OperationResidentComponent.Listener, DfCommProblem.Listener, DfCommWait.Listener {

    private static final int GOOGLE_SIGN_IN = 9001;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    @Inject
    Lazy<ResLoginGoogle> mResLoginGoogleLazy;

    private GoogleApiClient mGoogleApiClient;
    private ActivityResult mActivityResult;


    private GoogleApiClient.OnConnectionFailedListener mGoogleConnectionFailedListener =
            new GoogleApiClient.OnConnectionFailedListener() {

                @Override
                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                    mGoogleApiClient = null;
                }
            };


    private GoogleApiClient.ConnectionCallbacks mGoogleConnectionCallbacks =
            new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                    startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
                }


                @Override
                public void onConnectionSuspended(int i) {
                    // empty
                }
            };


    @NonNull
    @Override
    public ResLoginGoogle createResidentComponent() {
        return mResLoginGoogleLazy.get();
    }


    @Override
    public void onCommProblemClosed() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }


    @Override
    public void onCommWaitDialogCancelled() {
        getRes().abort();
    }


    @Override
    protected void handleResidentIdleState() {
        if (mActivityResult != null) {
            handleActivityResult();
            mActivityResult = null;
        }
    }


    @Override
    protected void handleResidentBusyState() {
        MyAppDialogs.showLoggingInDialog(getFragmentManager());
    }


    @Override
    protected void handleResidentEndedState() {
        MyAppDialogs.hideLoggingInDialog(getFragmentManager());
        if (getRes().isSuccess()) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());

        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login_google);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_id))
                .requestEmail()
                .build();


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(mGoogleConnectionCallbacks)
                .addOnConnectionFailedListener(mGoogleConnectionFailedListener)
                .build();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mActivityResult = new ActivityResult(requestCode, resultCode, data);
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
    }
}
