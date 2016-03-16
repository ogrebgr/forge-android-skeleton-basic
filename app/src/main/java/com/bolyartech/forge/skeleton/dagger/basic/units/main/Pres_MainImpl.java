package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;

import com.bolyartech.forge.android.app_unit.ActivityResultContainer;
import com.bolyartech.forge.android.app_unit.StateChangedEvent;
import com.bolyartech.forge.android.mvp.PresenterImpl;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Pres_MainImpl extends PresenterImpl<Mod_Main, P2V_Main, Host_Main> implements V2P_Main {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    @Inject
    Bus mBus;

    public Pres_MainImpl(Mod_Main resident, P2V_Main p2v, Host_Main host) {
        super(resident, p2v, host);
    }


    @Override
    public void onCreate() {

    }


    @Override
    public void onResume(ActivityResultContainer container) {
        mBus.register(this);
    }


    @Override
    public void onPause() {
        mBus.unregister(this);
    }


    @Override
    public void onDestroy() {

    }


    @Override
    public void onLoginButtonClicked() {
        if (mLoginPrefs.isManualRegistration()) {
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
                if (mNetworkInfoProvider.isConnected()) {
                    if (getSession().isLoggedIn()) {
                        getP2V().screenModeLoggedIn();
                    } else {
                        getP2V().screenModeNotLoggedIn();
                    }
                } else {
                    getP2V().screenModeNoInet();
                }

                break;
            case AUTO_REGISTERING:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case REGISTER_AUTO_FAIL:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                getModel().resetState();
                break;
            case SESSION_STARTED_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                getP2V().screenModeLoggedIn();
                getModel().resetState();
                break;
            case SESSION_START_FAIL:
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
            case LOGGING_IN:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case LOGIN_FAIL:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
                break;
            case LOGIN_INVALID:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showInvalidAutologinDialog(getFragmentManager());
                getModel().resetState();
                getP2V().screenModeNotLoggedIn();
            case UPGRADE_NEEDED:
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                break;
        }
    }


    @Subscribe
    public void onStateChangedEvent(StateChangedEvent ev) {
        handleState(mResident.getState());
    }
}
