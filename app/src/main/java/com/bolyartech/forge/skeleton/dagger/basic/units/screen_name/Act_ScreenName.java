package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.StateChangedEvent;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


public class Act_ScreenName extends SessionActivity implements Df_ScreenNameOk.Listener {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private EditText mEtScreenName;

    @Inject
    Session mSession;

    @Inject
    Provider<Res_ScreenNameImpl> mRes_ScreenNameImplProvider;

    private Res_ScreenName mResident;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DependencyInjector.getInstance().inject(this);
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
                    mResident.screenName(mEtScreenName.getText().toString());
                } else {
                    mEtScreenName.setError(getString(R.string.act__screen_name__et_screen_name_missing));
                }
            }
        });
    }


    @Override
    public ResidentComponent createResidentComponent() {
        return mRes_ScreenNameImplProvider.get();
    }


    @Override
    public void onResume() {
        super.onResume();

        mResident = (Res_ScreenName) getResidentComponent();

        handleState(mResident.getState());
    }


    private void handleState(Res_ScreenName.State state) {
        switch (state) {
            case IDLE:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                break;
            case PROCESSING:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case SCREEN_NAME_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                showScreenNameOkDialog(getFragmentManager());
                break;
            case SCREEN_NAME_FAIL:
                handleError();
                break;
        }
    }


    private void handleError() {
        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
        if (mResident.getLastError() != null) {
            switch (mResident.getLastError()) {
                case INVALID_SCREEN_NAME:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_invalid));
                    mResident.resetState();
                    break;
                case SCREEN_NAME_EXISTS:
                    mEtScreenName.setError(getString(R.string.act__register__et_screen_name_error_taken));
                    mResident.resetState();
                    break;
                case SCREEN_NAME_CHANGE_NOT_SUPPORTED:
                    // this should not happen
                    finish();
                    break;
                default:
                    mLogger.error("Unexpected error: {}", mResident.getLastError());
                    break;
            }

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


    @Subscribe
    public void onStateChangedEvent(StateChangedEvent ev) {
        handleState(mResident.getState());
    }
}
