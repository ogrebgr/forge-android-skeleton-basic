package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.app_unit.ResidentComponent;
import static com.bolyartech.forge.misc.ViewUtils.*;

import com.bolyartech.forge.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_Register extends SessionActivity implements DoesLogin {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Inject
    Provider<Res_RegisterImpl> mRes_RegisterImplProvider;

    private EditText mEtUsername;
    private EditText mEtPassword;
    private EditText mEtScreenName;

    private Res_Register mResident;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSession() != null && getSession().isLoggedIn()) {
            mLogger.error("Already logged in. Unlog first before attempting registration.");
            finish();
        }

        setContentView(R.layout.act__register);
        getDependencyInjector().inject(this);

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
                    mResident.register(mEtUsername.getText().toString(),
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
    public ResidentComponent createResidentComponent() {
        return mRes_RegisterImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        mResident = (Res_Register) getResidentComponent();

        handleState(mResident.getState());
    }


    @Subscribe
    public void onEv_StateChanged(Ev_StateChanged ev) {
        handleState(mResident.getState());
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
                finish();
                break;
            case REGISTER_FAIL:
                handleError();
                break;
        }
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        if (mResident.getLastError() != null) {
            switch (mResident.getLastError()) {
                case UPGRADE_NEEDED:
                    MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                    break;
                case INVALID_USERNAME:
                    mEtUsername.setError(getString(R.string.act__register__et_username_error_invalid));
                    mResident.resetState();
                    break;
                case USERNAME_EXISTS:
                    mEtUsername.setError(getString(R.string.act__register__et_username_error_taken));
                    mResident.resetState();
                    break;
                case INVALID_PASSWORD:
                    mEtPassword.setError(getString(R.string.act__register__et_password_error_invalid));
                    mResident.resetState();
                    break;
                case INVALID_SCREEN_NAME:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                    mResident.resetState();
                    break;
                case SCREEN_NAME_EXISTS:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                    mResident.resetState();
                    break;
                default:
                    mLogger.error("Unexpected error code: {}", mResident.getLastError());
                    MyAppDialogs.showCommProblemDialog(getFragmentManager());
                    break;
            }
        } else {
            mLogger.error("Error code NULL");
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }

}
