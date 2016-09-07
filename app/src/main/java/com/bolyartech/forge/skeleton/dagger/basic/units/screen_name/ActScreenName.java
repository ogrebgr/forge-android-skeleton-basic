package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthorizationResponseCodes;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActScreenName extends SessionActivity<RtaScreenName> implements OperationResidentComponent.Listener,
        DfScreenNameOk.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private EditText mEtScreenName;

    @Inject
    Session mSession;

    @Inject
    Lazy<ResScreenNameImpl> mRes_ScreenNameImplLazy;

    @Inject
    CurrentUserHolder mCurrentUserHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDependencyInjector().inject(this);

        CurrentUser user = mCurrentUserHolder.getCurrentUser();
        if (TextUtils.isEmpty(user.getScreenName())) {
            setContentView(R.layout.act__screen_name);

            View view = getWindow().getDecorView();

            initViews(view);
        } else {
            mLogger.error("No session info or already have screen name. Finishing...");
            finish();
        }
    }


    private void initViews(View view) {
        mEtScreenName = ViewUtils.findEditTextX(view, R.id.et_screen_name);

        ViewUtils.initButton(view, R.id.btn_save, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isNotEmpty(mEtScreenName.getText().toString())) {
                    getRi().screenName(mEtScreenName.getText().toString());
                } else {
                    mEtScreenName.setError(getString(R.string.act__screen_name__et_screen_name_missing));
                }
            }
        });
    }


    @NonNull
    @Override
    public ResScreenName createResidentComponent() {
        return mRes_ScreenNameImplLazy.get();
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
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                if (getRi().isSuccess()) {
                    showScreenNameOkDialog(getFragmentManager());
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
            if (error == AuthorizationResponseCodes.Errors.INVALID_SCREEN_NAME.getCode()) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                getRi().completedStateAcknowledged();
            } else if (error == AuthorizationResponseCodes.Errors.SCREEN_NAME_EXISTS.getCode()) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                getRi().completedStateAcknowledged();
            } else if (error == AuthorizationResponseCodes.Errors.SCREEN_NAME_CHANGE_NOT_SUPPORTED.getCode()) {
                mLogger.error("SCREEN_NAME_CHANGE_NOT_SUPPORTED");
                finish();
            } else {
                mLogger.error("Unexpected error: {}", getRi().getLastError());
                finish();
            }
        }
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }


    public static void showScreenNameOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfScreenNameOk.DIALOG_TAG) == null) {
            DfScreenNameOk fra = new DfScreenNameOk();
            fra.show(fm, DfScreenNameOk.DIALOG_TAG);
        }
    }


    @Override
    public void onResidentOperationStateChanged() {
        handleState(getRi().getOpState());
    }
}
