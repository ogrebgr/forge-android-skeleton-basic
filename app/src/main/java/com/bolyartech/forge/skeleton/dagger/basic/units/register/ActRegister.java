package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.OpSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static com.bolyartech.forge.android.misc.ViewUtils.findEditTextX;
import static com.bolyartech.forge.android.misc.ViewUtils.findViewX;
import static com.bolyartech.forge.android.misc.ViewUtils.initButton;


public class ActRegister extends OpSessionActivity<ResRegister> implements PerformsLogin,
        DfCommProblem.Listener, DfRegisterOk.Listener {

    
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    ResRegister mResRegister;

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
        return mResRegister;
    }


    @Override
    public void onResume() {
        super.onResume();

        handleState();
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
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn() && mLoginPrefs.isManualRegistration()) {
            mLogger.error("Already logged in with another manual reg. Logout first before attempting registration.");
            finish();
        }

        setContentView(R.layout.act__register);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View view = getWindow().getDecorView();
        initViews(view);
    }


    protected void handleState() {
        OperationResidentComponent.OpState opState = getRes().getOpState();

        switch (opState) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case ENDED:
                if (getRes().isSuccess()) {
                    MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                    setResult(Activity.RESULT_OK);
                    showRegisterOkDialog(getFragmentManager());
                } else {
                    handleError();
                }
                break;
        }
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
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        Integer error = getRes().getLastError();

        if (error != null) {
            if (error == BasicResponseCodes.Errors.UPGRADE_NEEDED) {
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
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
                mLogger.error("Unexpected error code: {}", getRes().getLastError());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
            }
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }
}
