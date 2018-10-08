package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.RctSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;

import static com.bolyartech.forge.android.misc.ViewUtils.findEditTextX;
import static com.bolyartech.forge.android.misc.ViewUtils.findViewX;
import static com.bolyartech.forge.android.misc.ViewUtils.initButton;


public class ActRegister extends RctSessionActivity<ResRegister> implements PerformsLogin,
        DfCommProblem.Listener, DfRegisterOk.Listener, DfCommWait.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    @Inject
    Lazy<ResRegister> mResRegisterLazy;

    @Inject
    LoginPrefs mLoginPrefs;

    @Inject
    CurrentUserHolder mCurrentUserHolder;


    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtScreenName;


    public static void showRegisterOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfRegisterOk.DIALOG_TAG) == null) {
            DfRegisterOk fra = new DfRegisterOk();
            fra.show(fm, DfRegisterOk.DIALOG_TAG);
        }
    }


    @NonNull
    @Override
    public ResRegister createResidentComponent() {
        return mResRegisterLazy.get();
    }


    @Override
    public void onCommProblemClosed() {
        finish();
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }


    @Override
    public void handleResidentEndedState() {
        if (getRes().getCurrentTask() != null) {
            if (getRes().getCurrentTask().isSuccess()) {
                MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());
                setResult(Activity.RESULT_OK);
                showRegisterOkDialog(getSupportFragmentManager());
            } else {
                handleError();
            }
        }
    }


    @Override
    public void handleResidentBusyState() {
        MyAppDialogs.showCommWaitDialog(getSupportFragmentManager());
    }


    @Override
    public void handleResidentIdleState() {
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());
    }


    @Override
    public void onCommWaitDialogCancelled() {
        getRes().abort();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn() && mLoginPrefs.isManualRegistration()) {
            mLogger.error("Already logged in with another manual reg. Logout first before attempting registration.");
            finish();
        }

        setContentView(R.layout.act__register);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View view = getWindow().getDecorView();
        initViews(view);
    }


    private void initViews(View view) {
        mEtUsername = findEditTextX(view, R.id.et_username);
        mEtPassword = findEditTextX(view, R.id.et_password);
        mEtScreenName = findEditTextX(view, R.id.et_screen_name);

        initButton(view, R.id.btn_register, v -> {
            if (isDataValid()) {
                getRes().register(mEtUsername.getText().toString(),
                        mEtPassword.getText().toString(),
                        mEtScreenName.getText().toString());
            }
        });


        if (mCurrentUserHolder.getCurrentUser() != null && mCurrentUserHolder.getCurrentUser().hasScreenName()) {
            View v = findViewX(view, R.id.v_screen_name);
            v.setVisibility(View.GONE);
        }
    }


    private boolean isDataValid() {
        if (StringUtils.isEmpty(mEtUsername.getText().toString())) {
            mEtUsername.setError(getString(R.string.act__register__et_username_error_missing));
            return false;
        }

        if (StringUtils.isEmpty(mEtPassword.getText().toString())) {
            mEtPassword.setError(getString(R.string.act__register__et_password_error_missing));
            return false;
        }

        if (!mCurrentUserHolder.getCurrentUser().hasScreenName()) {
            if (StringUtils.isEmpty(mEtScreenName.getText().toString())) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_missing));
                return false;
            }
        }

        return true;
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());

        int error = getRes().getLastError();

        if (error == BasicResponseCodes.Errors.UPGRADE_NEEDED) {
            MyAppDialogs.showUpgradeNeededDialog(getSupportFragmentManager());
        } else if (error == AuthenticationResponseCodes.Errors.INVALID_USERNAME) {
            mEtUsername.setError(getString(R.string.act__register__et_username_error_invalid));
            getRes().endedStateAcknowledged();
        } else if (error == AuthenticationResponseCodes.Errors.USERNAME_EXISTS) {
            mEtUsername.setError(getString(R.string.act__register__et_username_error_taken));
            getRes().endedStateAcknowledged();
        } else if (error == AuthenticationResponseCodes.Errors.INVALID_PASSWORD) {
            mEtPassword.setError(getString(R.string.act__register__et_password_error_invalid));
            getRes().endedStateAcknowledged();
        } else if (error == AuthenticationResponseCodes.Errors.INVALID_SCREEN_NAME) {
            mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
            getRes().endedStateAcknowledged();
        } else if (error == AuthenticationResponseCodes.Errors.SCREEN_NAME_EXISTS) {
            mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
            getRes().endedStateAcknowledged();
        } else {
            MyAppDialogs.showCommProblemDialog(getSupportFragmentManager());
        }
    }
}
