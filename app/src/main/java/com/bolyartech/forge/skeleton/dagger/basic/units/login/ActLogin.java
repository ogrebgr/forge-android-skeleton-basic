package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.RctSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfLoggingIn;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActLogin extends RctSessionActivity<ResLogin> implements PerformsLogin, DfLoggingIn.Listener {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    Lazy<ResLogin> mResLoginLazy;


    @Inject
    LoginPrefs mLoginPrefs;


    private EditText mEtUsername;
    private EditText mEtPassword;


    @Override
    public void onLoggingInDialogCancelled() {
        getRes().abort();
    }


    @NonNull
    @Override
    public ResLogin createResidentComponent() {
        return mResLoginLazy.get();
    }


    @Override
    public void handleResidentIdleState() {
        MyAppDialogs.hideLoggingInDialog(getSupportFragmentManager());
    }


    @Override
    public void handleResidentBusyState() {
        MyAppDialogs.showLoggingInDialog(getSupportFragmentManager());
    }


    @Override
    public void handleResidentEndedState() {
        MyAppDialogs.hideLoggingInDialog(getSupportFragmentManager());

        if (getRes().getLoginTaskResult().isSuccess()) {
            logger.debug("goin HOME");
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            handleError();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);

        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn()) {
            logger.error("Already logged in. Logout first before attempting new login.");
            finish();
        }

        setContentView(R.layout.act__login);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View view = getWindow().getDecorView();
        initViews(view);
    }


    private void initViews(View view) {
        mEtUsername = ViewUtils.findEditTextX(view, R.id.et_username);
        mEtPassword = ViewUtils.findEditTextX(view, R.id.et_password);

        if (mLoginPrefs.isManualRegistration()) {
            mEtUsername.setText(mLoginPrefs.getUsername());
            mEtPassword.setText(mLoginPrefs.getPassword());
        }


        ViewUtils.initButton(view, R.id.btn_login, v -> {
            if (isDataValid()) {
                getRes().login(mEtUsername.getText().toString(), mEtPassword.getText().toString());
            }
        });
    }


    @SuppressWarnings("RedundantIfStatement")
    private boolean isDataValid() {
        if (StringUtils.isEmpty(mEtUsername.getText().toString())) {
            return false;
        }

        if (StringUtils.isEmpty(mEtPassword.getText().toString())) {
            return false;
        }

        return true;
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());

        switch (getRes().getLoginTaskResult().getErrorValue()) {
            case AuthenticationResponseCodes.Errors.INVALID_LOGIN:
                MyAppDialogs.showInvalidAutologinDialog(getSupportFragmentManager());
                break;
            case BasicResponseCodes.Errors.UPGRADE_NEEDED:
                MyAppDialogs.showUpgradeNeededDialog(getSupportFragmentManager());
                break;
            default:
                MyAppDialogs.showCommProblemDialog(getSupportFragmentManager());
                break;
        }
    }
}
