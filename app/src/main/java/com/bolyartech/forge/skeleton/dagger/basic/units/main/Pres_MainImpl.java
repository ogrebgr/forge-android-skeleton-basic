package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.ActivityResultContainer;
import com.bolyartech.forge.android.app_unit.StateChangedEvent;
import com.bolyartech.forge.android.mvp.PresenterImpl;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Pres_MainImpl extends PresenterImpl<Mod_Main, P2V_Main, Host_Main> implements V2P_Main {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());



    @Inject
    public Pres_MainImpl(Mod_Main model, P2V_Main p2v, Host_Main host) {
        super(model, p2v, host);
    }


    void attachP2v(P2V_Main p2v) {
        mP2V = p2v;
    }


    @Override
    public void onCreate() {
    }


    @Override
    public void onResume(ActivityResultContainer container) {
    }


    @Override
    public void onPause() {
    }


    @Override
    public void onDestroy() {
    }


    @Override
    public void onLoginButtonClicked() {
        if (getModel().isManualRegistration()) {
            getHost().startLoginActivity();
        } else {
            getModel().login();
        }
    }


    @Override
    public void onRegisterButtonClicked() {

    }


    private synchronized void handleState(Mod_Main.State state) {
        mLogger.debug("State: {}", state);

        getP2V().invalidateOptionsMenu();
        switch (state) {
            case IDLE:
                if (getModel().isOnline()) {
                    if (getModel().isLoggedIn()) {
                        getP2V().screenModeLoggedIn();
                    } else {
                        getP2V().screenModeNotLoggedIn();
                    }
                } else {
                    getP2V().screenModeNoInet();
                }

                break;
            case AUTO_REGISTERING:
                getHost().showCommWaitDialog();
                break;
            case REGISTER_AUTO_FAIL:
                getHost().hideCommWaitDialog();
                getHost().showCommProblemDialog();
                getModel().resetState();
                break;
            case SESSION_STARTED_OK:
                getHost().hideCommWaitDialog();
                getHost().hideLoggingInDialog();
                getP2V().screenModeLoggedIn();
                getModel().resetState();
                break;
            case SESSION_START_FAIL:
                getHost().showCommProblemDialog();
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
            case LOGGING_IN:
                getHost().showLoggingInDialog();
                break;
            case LOGIN_FAIL:
                getHost().hideLoggingInDialog();
                getHost().showCommProblemDialog();
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
                break;
            case LOGIN_INVALID:
                getHost().hideLoggingInDialog();
                getHost().showInvalidAutologinDialog();
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
            case UPGRADE_NEEDED:
                getHost().showUpgradeNeededDialog();
                break;
        }
    }


    @Subscribe
    public void onStateChangedEvent(StateChangedEvent ev) {
        handleState(getModel().getState());
    }
}


