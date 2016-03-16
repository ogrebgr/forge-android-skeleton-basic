package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.app_unit.StateChangedEvent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.Df_CommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.Act_ScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.Act_SelectLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.Act_Register;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class Act_Main extends SessionActivity implements DoesLogin, Df_CommWait.Listener {
    private static final int ACT_SELECT_LOGIN = 1;
    private static final int ACT_REGISTER = 2;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    @Inject
    NetworkInfoProvider mNetworkInfoProvider;

    @Inject
    LoginPrefs mLoginPrefs;

    @Inject
    Provider<Res_MainImpl> mRes_MainImplProvider;

    private Mod_Main mResident;

    private ConnectivityChangeReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();

    private volatile Runnable mOnResumePendingAction;


    @Override
    public ResidentComponent createResidentComponent() {
        return mRes_MainImplProvider.get();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act__main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DependencyInjector.getInstance().inject(this);

        initViews();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act__main, menu);

        if (getSession().isLoggedIn()) {
            if (getSession().getInfo() != null && !getSession().getInfo().hasScreenName()) {
                menu.findItem(R.id.ab_screen_name).setVisible(true);
            }

            if (!mLoginPrefs.isManualRegistration()) {
                menu.findItem(R.id.ab_full_registration).setVisible(true);
            }
            menu.findItem(R.id.ab_logout).setVisible(true);
        } else {
            menu.findItem(R.id.ab_registration).setVisible(true);
        }



        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ab_logout) {
            mResident.logout();
            if (FacebookSdk.isInitialized()) {
                LoginManager.getInstance().logOut();
            }
        } else if (id == R.id.ab_select_login) {
            Intent intent = new Intent(this, Act_SelectLogin.class);
            startActivityForResult(intent, ACT_SELECT_LOGIN);
        } else if (id == R.id.ab_screen_name) {
            Intent intent = new Intent(Act_Main.this, Act_ScreenName.class);
            startActivity(intent);
        } else if (id == R.id.ab_registration || id == R.id.ab_full_registration) {
            Intent intent = new Intent(Act_Main.this, Act_Register.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


    private void initViews() {
        View view = getWindow().getDecorView();

ss
    }


    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        mResident = (Mod_Main) getResidentComponent();

        if (mOnResumePendingAction == null) {
            handleState(mResident.getState());
        } else {
            runOnUiThread(mOnResumePendingAction);
        }
    }


    private synchronized void handleState(Mod_Main.State state) {
        mLogger.debug("State: {}", state);
        invalidateOptionsMenu();
        switch (state) {
            case IDLE:
                if (mNetworkInfoProvider.isConnected()) {
                    if (getSession().isLoggedIn()) {
                        screenModeLoggedIn();
                    } else {
                        screenModeNotLoggedIn();
                    }
                } else {
                    screenModeNoInet();
                }

                break;
            case AUTO_REGISTERING:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case REGISTER_AUTO_FAIL:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                mResident.resetState();
                break;
            case SESSION_STARTED_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                screenModeLoggedIn();
                mResident.resetState();
                break;
            case SESSION_START_FAIL:
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                mResident.resetState();
                screenModeNotLoggedIn();
            case LOGGING_IN:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case LOGIN_FAIL:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                mResident.resetState();
                screenModeNotLoggedIn();
                break;
            case LOGIN_INVALID:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showInvalidAutologinDialog(getFragmentManager());
                mResident.resetState();
                screenModeNotLoggedIn();
            case UPGRADE_NEEDED:
                MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                break;
        }
    }


    private void screenModeNoInet() {
        mViewNoInet.setVisibility(View.VISIBLE);
        mViewNotLoggedIn.setVisibility(View.GONE);
        mViewLoggedIn.setVisibility(View.GONE);
    }


    private void screenModeNotLoggedIn() {
        mViewNoInet.setVisibility(View.GONE);
        mViewNotLoggedIn.setVisibility(View.VISIBLE);
        mViewLoggedIn.setVisibility(View.GONE);

        mBtnLogin.setVisibility(View.VISIBLE);
        if (!mLoginPrefs.isManualRegistration()) {
            mBtnRegister.setVisibility(View.VISIBLE);
        } else {
            mBtnRegister.setVisibility(View.GONE);
        }
    }


    private void screenModeLoggedIn() {
        mViewNoInet.setVisibility(View.GONE);
        mViewNotLoggedIn.setVisibility(View.GONE);
        mViewLoggedIn.setVisibility(View.VISIBLE);

        mBtnLogin.setVisibility(View.GONE);
        mBtnRegister.setVisibility(View.GONE);

        Session.Info info = getSession().getInfo();
        if (info.hasScreenName()) {
            mTvLoggedInAs.setText(Html.fromHtml(String.format(getString(R.string.act__main__tv_logged_in), info.getScreenName())));
        } else {
            mTvLoggedInAs.setText(Html.fromHtml(String.format(getString(R.string.act__main__tv_logged_in_auto), info.getUserId())));
        }
    }


    @Subscribe
    public void onStateChangedEvent(StateChangedEvent ev) {
        handleState(mResident.getState());
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    @Override
    public void onCommWaitDialogCancelled() {
        mResident.abortLogin();
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mResident != null) {
                mResident.onConnectivityChange();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACT_REGISTER) {
            if (resultCode == Activity.RESULT_OK) {
                mOnResumePendingAction = new Runnable() {
                    @Override
                    public void run() {
                        mOnResumePendingAction = null;
                        mResident.startSession();
                    }
                };
            }
        }
    }
}
