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
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.Df_CommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;

import static com.bolyartech.forge.android.misc.ViewUtils.findEditTextX;
import static com.bolyartech.forge.android.misc.ViewUtils.initButton;


public class ActRegister extends SessionActivity<ResRegister> implements PerformsLogin,
        OperationResidentComponent.Listener, Df_CommProblem.Listener, DfRegisterOk.Listener {

    
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Lazy<ResRegisterImpl> mRes_RegisterImplLazy;

    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtScreenName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn()) {
            mLogger.error("Already logged in. Logout first before attempting registration.");
            finish();
        }

        setContentView(R.layout.act__register);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        View view = getWindow().getDecorView();
        initViews(view);
    }


    private void initViews(View view) {
        mEtUsername = findEditTextX(view, R.id.et_username);
        mEtPassword = findEditTextX(view, R.id.et_password);
        mEtScreenName = findEditTextX(view, R.id.et_screen_name);

        initButton(view, R.id.btn_register, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDataValid()) {
                    getRes().register(mEtUsername.getText().toString(),
                            mEtPassword.getText().toString(),
                            mEtScreenName.getText().toString());
                }
            }
        });
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

        if (StringUtils.isEmpty(mEtScreenName.getText().toString())) {
            mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_missing));
            return false;
        }

        return true;
    }


    @NonNull
    @Override
    public ResRegister createResidentComponent() {
        return mRes_RegisterImplLazy.get();
    }


    @Override
    public void onResume() {
        super.onResume();


        handleState(getRes().getOpState());
    }


    @Override
    public void onResidentOperationStateChanged() {
        runOnUiThread(() -> handleState(getRes().getOpState()));
    }


    private void handleState(OperationResidentComponent.OpState opState) {
        switch(opState) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case COMPLETED:
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


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        Integer error = getRes().getLastError();

        if (error != null) {
            if (error == BasicResponseCodes.Errors.UPGRADE_NEEDED) {
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
            } else if (error == AuthenticationResponseCodes.Errors.INVALID_USERNAME) {
                mEtUsername.setError(getString(R.string.act__register__et_username_error_invalid));
                getRes().completedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.USERNAME_EXISTS) {
                mEtUsername.setError(getString(R.string.act__register__et_username_error_taken));
                getRes().completedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.INVALID_PASSWORD) {
                mEtPassword.setError(getString(R.string.act__register__et_password_error_invalid));
                getRes().completedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.INVALID_SCREEN_NAME) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                getRes().completedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.SCREEN_NAME_EXISTS) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                getRes().completedStateAcknowledged();
            } else {
                mLogger.error("Unexpected error code: {}", getRes().getLastError());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
            }
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }


    public static void showRegisterOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfRegisterOk.DIALOG_TAG) == null) {
            DfRegisterOk fra = new DfRegisterOk();
            fra.show(fm, DfRegisterOk.DIALOG_TAG);
        }
    }


    @Override
    public void onCommProblemClosed() {
        finish();
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }
}
