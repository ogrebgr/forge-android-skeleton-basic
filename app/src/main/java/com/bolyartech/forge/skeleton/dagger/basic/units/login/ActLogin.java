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
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class ActLogin extends SessionActivity<RiLogin> implements OperationResidentComponent.Listener,
        PerformsLogin {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Provider<ResLoginImpl> mRes_LoginImplProvider;


    @Inject
    LoginPrefs mLoginPrefs;


    private EditText mEtUsername;
    private EditText mEtPassword;


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


        ViewUtils.initButton(view, R.id.btn_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataValid()) {
                    getRi().login(mEtUsername.getText().toString(), mEtPassword.getText().toString());
                }
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
        return mRes_LoginImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        handleState(getRi().getOpState());
    }


    private void handleState(OperationResidentComponent.OpState opState) {
        switch (opState) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case COMPLETED:
                if (getRi().isSuccess()) {
                    MyAppDialogs.hideCommWaitDialog(getFragmentManager());
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

        Integer error = getRi().getLastError();
        if (error != null) {
            if (error == BasicResponseCodes.Errors.UPGRADE_NEEDED.getCode()) {
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
            } else {
                mLogger.error("Unexpected error code: {}", getRi().getLastError());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
            }
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }


    @Override
    public void onResidentOperationStateChanged() {
        handleState(getRi().getOpState());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
