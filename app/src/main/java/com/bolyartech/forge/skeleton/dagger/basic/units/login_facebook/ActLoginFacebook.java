package com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class ActLoginFacebook extends SessionActivity<ResLoginFacebook> implements PerformsLogin,
        OperationResidentComponent.Listener, DfCommProblem.Listener, DfCommWait.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());
    @Inject
    ResLoginFacebook mResLoginFacebook;
    private AccessToken mAccessToken;
    private CallbackManager mFacebookCallbackManager;


    @Override
    public void onResidentOperationStateChanged() {
        handleState();
    }


    @Override
    public void onResume() {
        super.onResume();

        if (mAccessToken != null) {
            AccessToken tmp = mAccessToken;
            mAccessToken = null;
            getRes().checkFbLogin(tmp.getToken());
        }

        handleState();
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


    @NonNull
    @Override
    public ResLoginFacebook createResidentComponent() {
        return mResLoginFacebook;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login_facebook);

        initializeFacebookSdk();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void handleState() {
        OperationResidentComponent.OpState opState = getRes().getOpState();
        mLogger.debug("State: " + opState);
        switch (opState) {
            case IDLE:
                break;
            case BUSY:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case ENDED:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                if (getRes().isSuccess()) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    MyAppDialogs.showCommProblemDialog(getFragmentManager());

                }
                getRes().ack();
                break;
        }
    }


    private void initializeFacebookSdk() {
        if (!FacebookSdk.isInitialized()) {
            FacebookSdk.sdkInitialize(getApplicationContext(), () -> {
                initFacebookCallback();
                mAccessToken = AccessToken.getCurrentAccessToken();
                if (mAccessToken == null) {
                    LoginManager.getInstance().logInWithReadPermissions(this, null);
                }
            });
        } else {
            initFacebookCallback();
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
}
