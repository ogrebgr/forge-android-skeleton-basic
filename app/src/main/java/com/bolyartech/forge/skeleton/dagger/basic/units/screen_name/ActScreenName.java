package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.RctSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActScreenName extends RctSessionActivity<ResScreenName> implements DfScreenNameOk.Listener,
        DfCommProblem.Listener, DfCommWait.Listener {


    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());

    @Inject
    Session mSession;

    @Inject
    Lazy<ResScreenName> mResScreenNameLazy;

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
        return mResScreenNameLazy.get();
    }


    @Override
    public void onScreenNameOkDialogClosed() {
        finish();
    }


    @Override
    public void handleResidentIdleState() {
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());
    }


    @Override
    public void handleResidentBusyState() {
        MyAppDialogs.showCommWaitDialog(getSupportFragmentManager());
    }


    @Override
    public void handleResidentEndedState() {
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());
        if (getRes().getCurrentTask() != null) {
            if (getRes().getCurrentTask().isSuccess()) {
                showScreenNameOkDialog(getSupportFragmentManager());
            } else {
                handleError();
            }
        }
    }


    @Override
    public void onCommWaitDialogCancelled() {
        // TODO - server may update screen name if the request is cancelled too late. Reload data on cancel to avoid OOS?
        getRes().abort();
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
        MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());

        int error = getRes().getLastError();

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
            MyAppDialogs.showCommProblemDialog(getSupportFragmentManager());
            finish();
        }
    }
}
