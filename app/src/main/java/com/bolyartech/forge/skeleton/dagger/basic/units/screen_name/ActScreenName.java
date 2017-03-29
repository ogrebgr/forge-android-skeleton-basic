package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.OpSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class ActScreenName extends OpSessionActivity<ResScreenName> implements DfScreenNameOk.Listener,
        DfCommProblem.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());
    @Inject
    Session mSession;
    @Inject
    ResScreenName mResScreenName;
    @Inject
    CurrentUserHolder mCurrentUserHolder;
    private EditText mEtScreenName;


    public static void showScreenNameOkDialog(FragmentManager fm) {
        if (fm.findFragmentByTag(DfScreenNameOk.DIALOG_TAG) == null) {
            DfScreenNameOk fra = new DfScreenNameOk();
            fra.show(fm, DfScreenNameOk.DIALOG_TAG);
        }
    }


    @Override
    public void onCommProblemClosed() {
        finish();
    }


    @NonNull
    @Override
    public ResScreenName createResidentComponent() {
        return mResScreenName;
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }


    @Override
    protected void handleResidentIdleState() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
    }


    @Override
    protected void handleResidentBusyState() {
        MyAppDialogs.showCommWaitDialog(getFragmentManager());
    }


    @Override
    protected void handleResidentEndedState() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        if (getRes().isSuccess()) {
            showScreenNameOkDialog(getFragmentManager());
        } else {
            handleError();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        CurrentUser user = mCurrentUserHolder.getCurrentUser();
        if (!user.hasScreenName()) {
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

        ViewUtils.initButton(view, R.id.btn_save, v -> {
            if (StringUtils.isNotEmpty(mEtScreenName.getText().toString())) {
                getRes().screenName(mEtScreenName.getText().toString());
            } else {
                mEtScreenName.setError(getString(R.string.act__screen_name__et_screen_name_missing));
            }
        });
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());

        Integer error = getRes().getLastError();

        if (error != null) {
            if (error == AuthenticationResponseCodes.Errors.INVALID_SCREEN_NAME) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                getRes().endedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.SCREEN_NAME_EXISTS) {
                mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                getRes().endedStateAcknowledged();
            } else if (error == AuthenticationResponseCodes.Errors.SCREEN_NAME_CHANGE_NOT_SUPPORTED) {
                mLogger.error("SCREEN_NAME_CHANGE_NOT_SUPPORTED");
                finish();
            } else {
                mLogger.error("Unexpected error: {}", getRes().getLastError());
                finish();
            }
        } else {
            MyAppDialogs.showCommProblemDialog(getFragmentManager());
        }
    }
}
