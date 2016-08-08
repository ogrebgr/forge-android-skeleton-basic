package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.Df_CommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.bolyartech.forge.android.misc.ViewUtils.findEditTextX;
import static com.bolyartech.forge.android.misc.ViewUtils.initButton;


public class Act_Register extends SessionActivity<Res_Register> implements DoesLogin,
        Df_CommProblem.Listener, Df_RegisterOk.Listener {

    
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Provider<Res_RegisterImpl> mRes_RegisterImplProvider;

    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtScreenName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn()) {
            mLogger.error("Already logged in. Unlog first before attempting registration.");
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
                    getResidentComponent().register(mEtUsername.getText().toString(),
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


    @Override
    public Res_Register createResidentComponent() {
        return mRes_RegisterImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();


        handleState(getResidentComponent().getState());
    }


    @Override
    public void stateChanged() {
        handleState(getResidentComponent().getState());
    }


    private void handleState(Res_Register.State state) {
        switch(state) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case REGISTERING:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case REGISTER_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                setResult(Activity.RESULT_OK);
                showRegisterOkDialog(getFragmentManager());
                break;
            case REGISTER_FAIL:
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
                case INVALID_USERNAME:
                    mEtUsername.setError(getString(R.string.act__register__et_username_error_invalid));
                    getResidentComponent().stateHandled();
                    break;
                case USERNAME_EXISTS:
                    mEtUsername.setError(getString(R.string.act__register__et_username_error_taken));
                    getResidentComponent().stateHandled();
                    break;
                case INVALID_PASSWORD:
                    mEtPassword.setError(getString(R.string.act__register__et_password_error_invalid));
                    getResidentComponent().stateHandled();
                    break;
                case INVALID_SCREEN_NAME:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                    getResidentComponent().stateHandled();
                    break;
                case SCREEN_NAME_EXISTS:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                    getResidentComponent().stateHandled();
                    break;
                default:
                    mLogger.error("Unexpected error code: {}", getResidentComponent().getLastError());
                    MyAppDialogs.showCommProblemDialog(getFragmentManager());
                    break;
            }
        } else {
            mLogger.error("Error code NULL");
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }


    public static void showRegisterOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(Df_RegisterOk.DIALOG_TAG) == null) {
            Df_RegisterOk fra = new Df_RegisterOk();
            fra.show(fm, Df_RegisterOk.DIALOG_TAG);
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
