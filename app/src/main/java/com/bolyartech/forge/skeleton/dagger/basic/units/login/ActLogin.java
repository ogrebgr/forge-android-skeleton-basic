package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.OpSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfLoggingIn;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActLogin extends OpSessionActivity<ResLogin> implements PerformsLogin, DfLoggingIn.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Lazy<ResLogin> mRes_LoginLazy;


    @Inject
    LoginPrefs mLoginPrefs;


    private EditText mEtUsername;
    private EditText mEtPassword;


    @Override
    public void onLoggingInDialogCancelled() {
        if (getRes().isBusy()) {
            getRes().abortLogin();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);

        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn()) {
            mLogger.error("Already logged in. Logout first before attempting new login.");
            finish();
        }

        setContentView(R.layout.act__login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
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


    @NonNull
    @Override
    public ResLogin createResidentComponent() {
        return mRes_LoginLazy.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        handleState();
    }


    protected void handleState() {
        OperationResidentComponent.OpState opState = getRes().getOpState();

        switch (opState) {
            case IDLE:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                break;
            case BUSY:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case ENDED:
                if (getRes().isSuccess()) {
                    MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    handleError();
                }
                break;
        }
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        Integer error = getRes().getLastError();
        if (error != null) {
            if (error == BasicResponseCodes.Errors.UPGRADE_NEEDED) {
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
            } else {
                mLogger.error("Unexpected error code: {}", getRes().getLastError());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
            }
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }
}
