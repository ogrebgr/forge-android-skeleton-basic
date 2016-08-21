package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthorizationResponseCodes;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_ScreenName extends SessionActivity<Res_ScreenName> implements OperationResidentComponent.Listener,
        Df_ScreenNameOk.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private EditText mEtScreenName;

    @Inject
    Session mSession;

    @Inject
    Provider<Res_ScreenNameImpl> mRes_ScreenNameImplProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDependencyInjector().inject(this);
        if (mSession.getInfo() != null && !mSession.getInfo().hasScreenName()) {
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
                    getResidentComponent().screenName(mEtScreenName.getText().toString());
                } else {
                    mEtScreenName.setError(getString(R.string.act__screen_name__et_screen_name_missing));
                }
            }
        });
    }


    @NonNull
    @Override
    public Res_ScreenName createResidentComponent() {
        return mRes_ScreenNameImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        handleState(getResidentComponent().getOperationState());
    }


    private void handleState(OperationResidentComponent.OperationState state) {
        switch (state) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case BUSY:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case COMPLETED:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                if (getResidentComponent().isSuccess()) {
                    showScreenNameOkDialog(getFragmentManager());
                } else {
                    handleError();
                }

                break;
        }
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        int error = getResidentComponent().getLastError();

        if (error == AuthorizationResponseCodes.Errors.INVALID_SCREEN_NAME.getCode()) {
            mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
            getResidentComponent().completedStateAcknowledged();
        } else if (error == AuthorizationResponseCodes.Errors.SCREEN_NAME_EXISTS.getCode()) {
            mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
            getResidentComponent().completedStateAcknowledged();
        } else if (error == AuthorizationResponseCodes.Errors.SCREEN_NAME_CHANGE_NOT_SUPPORTED.getCode()) {
            mLogger.error("SCREEN_NAME_CHANGE_NOT_SUPPORTED");
            finish();
        } else {
            mLogger.error("Unexpected error: {}", getResidentComponent().getLastError());
            finish();
        }
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }


    public static void showScreenNameOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(Df_ScreenNameOk.DIALOG_TAG) == null) {
            Df_ScreenNameOk fra = new Df_ScreenNameOk();
            fra.show(fm, Df_ScreenNameOk.DIALOG_TAG);
        }
    }


    @Override
    public void onResidentOperationStateChanged() {
        handleState(getResidentComponent().getOperationState());
    }
}
