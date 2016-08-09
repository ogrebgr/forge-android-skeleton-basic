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
import android.widget.Button;
import android.widget.TextView;

import com.bolyartech.forge.android.app_unit.StatefulResidentComponent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.Df_CommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.Act_ScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.Act_SelectLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.Act_Register;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class Act_Main extends SessionActivity<Res_Main> implements StatefulResidentComponent.Listener,
        DoesLogin, Df_CommWait.Listener {


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


    private ConnectivityChangeReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();

    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mTvLoggedInAs;

    private volatile Runnable mOnResumePendingAction;


    @Override
    public Res_Main createResidentComponent() {
        return mRes_MainImplProvider.get();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act__main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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
            getResidentComponent().logout();
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

        mViewNoInet = ViewUtils.findViewX(view, R.id.v_no_inet);
        mViewNotLoggedIn = ViewUtils.findViewX(view, R.id.v_not_logged_in);
        mViewLoggedIn = ViewUtils.findViewX(view, R.id.v_logged_in);

        mTvLoggedInAs = ViewUtils.findTextViewX(view, R.id.tv_logged_in_as);

        mBtnLogin = ViewUtils.initButton(view, R.id.btn_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLoginPrefs.isManualRegistration()) {
                    Intent intent = new Intent(Act_Main.this, Act_Login.class);
                    startActivity(intent);
                } else {
                    getResidentComponent().login();
                }
            }
        });

        mBtnRegister = ViewUtils.initButton(view, R.id.btn_register, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Act_Main.this, Act_Register.class);
                startActivityForResult(intent, ACT_REGISTER);
            }
        });


        if (getResources().getBoolean(R.bool.app_conf__do_autoregister)) {
            mBtnRegister.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (mOnResumePendingAction == null) {
            handleState(getResidentComponent().getState());
        } else {
            runOnUiThread(mOnResumePendingAction);
        }
    }


    private synchronized void handleState(Res_Main.State state) {
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
                getResidentComponent().stateHandled();
                break;
            case SESSION_STARTED_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                screenModeLoggedIn();
                getResidentComponent().stateHandled();
                break;
            case SESSION_START_FAIL:
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                getResidentComponent().stateHandled();
                screenModeNotLoggedIn();
                break;
            case LOGGING_IN:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case LOGIN_FAIL:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                getResidentComponent().stateHandled();
                screenModeNotLoggedIn();
                break;
            case LOGIN_INVALID:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showInvalidAutologinDialog(getFragmentManager());
                getResidentComponent().stateHandled();
                screenModeNotLoggedIn();
                break;
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


    @Override
    public void onResidentStateChanged() {
        handleState(getResidentComponent().getState());
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    @Override
    public void onCommWaitDialogCancelled() {
        getResidentComponent().abortLogin();
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (getResidentComponent() != null) {
                getResidentComponent().onConnectivityChange();
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
                        getResidentComponent().startSession();
                    }
                };
            }
        }
    }
}
