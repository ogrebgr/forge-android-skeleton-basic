package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.StateChangedEvent;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_Login extends SessionActivity<Res_Login> implements DoesLogin {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Provider<Res_LoginImpl> mRes_LoginImplProvider;


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
                    getResidentComponent().login(mEtUsername.getText().toString(), mEtPassword.getText().toString());
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


    @Override
    public Res_Login createResidentComponent() {
        return mRes_LoginImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        handleState(getResidentComponent().getState());
    }


    private void handleState(Res_Login.State state) {
        switch (state) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case LOGGING_IN:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case LOGIN_FAIL:
                break;
            case STARTING_SESSION:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case SESSION_STARTED_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                setResult(Activity.RESULT_OK);
                finish();
                break;
            case SESSION_START_FAIL:
                handleError();
                break;
        }
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        if (getResidentComponent().getLastError() != null) {
            switch (getResidentComponent().getLastError()) {
                case UPGRADE_NEEDED:
                    MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                    break;
            }
        }
    }


    @Override
    public void stateChanged() {
        handleState(getResidentComponent().getState());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
