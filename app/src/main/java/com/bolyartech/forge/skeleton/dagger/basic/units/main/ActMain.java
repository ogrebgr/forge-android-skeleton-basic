package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bolyartech.forge.android.misc.ActivityResult;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.OpSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ActLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ActScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ActSelectLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ActRegister;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class ActMain extends OpSessionActivity<ResMain> implements PerformsLogin, DfCommWait.Listener {


    private static final int ACT_SELECT_LOGIN = 1;
    private static final int ACT_REGISTER = 2;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    @Inject
    NetworkInfoProvider mNetworkInfoProvider;

    @Inject
    LoginPrefs mLoginPrefs;

    @Inject
    Lazy<ResMain> mRes_MainImplLazy;

    private ConnectivityChangeReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();

    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mTvLoggedInAs;

    private ActivityResult mActivityResult;


    @NonNull
    @Override
    public ResMain createResidentComponent() {
        return mRes_MainImplLazy.get();
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
            CurrentUser user = getRes().getCurrentUser();
            if (!TextUtils.isEmpty(user.getScreenName())) {
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
            getRes().logout();
            if (FacebookSdk.isInitialized()) {
                LoginManager.getInstance().logOut();
            }
            screenModeNotLoggedIn();
        } else if (id == R.id.ab_select_login) {
            Intent intent = new Intent(this, ActSelectLogin.class);
            startActivityForResult(intent, ACT_SELECT_LOGIN);
        } else if (id == R.id.ab_screen_name) {
            Intent intent = new Intent(ActMain.this, ActScreenName.class);
            startActivity(intent);
        } else if (id == R.id.ab_registration || id == R.id.ab_full_registration) {
            Intent intent = new Intent(ActMain.this, ActRegister.class);
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

        mBtnLogin = ViewUtils.initButton(view, R.id.btn_login, v -> {
            if (mLoginPrefs.isManualRegistration()) {
                Intent intent = new Intent(ActMain.this, ActLogin.class);
                startActivity(intent);
            } else {
                getRes().login();
            }
        });

        mBtnRegister = ViewUtils.initButton(view, R.id.btn_register, v -> {
            Intent intent = new Intent(ActMain.this, ActRegister.class);
            startActivityForResult(intent, ACT_REGISTER);
        });


        if (getResources().getBoolean(R.bool.app_conf__do_autoregister)) {
            mBtnRegister.setVisibility(View.GONE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (mActivityResult == null) {
            handleState();
        } else {
            if (mActivityResult.requestCode == ACT_REGISTER) {
                if (mActivityResult.resultCode == Activity.RESULT_OK) {
                    screenModeLoggedIn();
                }
            }
            mActivityResult = null;
        }
    }


    protected synchronized void handleState() {
        OperationResidentComponent.OpState opState = getRes().getOpState();

        mLogger.debug("State: {}", opState);
        invalidateOptionsMenu();


        switch (opState) {
            case IDLE:
                if (mNetworkInfoProvider.isConnected()) {
                    if (getSession().isLoggedIn()) {
                        screenModeLoggedIn();
                    } else {
                        screenModeNotLoggedIn();
                        getRes().autoLoginIfNeeded();
                    }
                } else {
                    screenModeNoInet();
                }

                break;
            case BUSY:
                switch(getRes().getCurrentOperation()) {
                    case AUTO_REGISTERING:
                        MyAppDialogs.showCommWaitDialog(getFragmentManager());
                        break;
                    case LOGIN:
                        MyAppDialogs.showLoggingInDialog(getFragmentManager());
                        break;
                }

                break;
            case COMPLETED:
                switch(getRes().getCurrentOperation()) {
                    case AUTO_REGISTERING:
                        MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                        if (getRes().isSuccess()) {
                            screenModeLoggedIn();
                        } else {
                            switch (getRes().getAutoregisteringError()) {
                                case FAILED:
                                    MyAppDialogs.showCommProblemDialog(getFragmentManager());
                                    break;
                                case UPGRADE_NEEDED:
                                    MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                                    break;
                            }
                        }
                        break;
                    case LOGIN:
                        MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                        if (getRes().isSuccess()) {
                            screenModeLoggedIn();
                        } else {
                            switch (getRes().getLoginError()) {
                                case INVALID_LOGIN:
                                    screenModeNotLoggedIn();
                                    MyAppDialogs.showInvalidAutologinDialog(getFragmentManager());
                                    break;
                                case FAILED:
                                    MyAppDialogs.showCommProblemDialog(getFragmentManager());
                                    screenModeNotLoggedIn();
                                    break;
                                case UPGRADE_NEEDED:
                                    MyAppDialogs.showUpgradeNeededDialog(getFragmentManager());
                                    break;
                            }
                        }

                        break;
                }

                getRes().completedStateAcknowledged();

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

        CurrentUser user = getRes().getCurrentUser();
        if (!TextUtils.isEmpty(user.getScreenName())) {
            //noinspection deprecation
            mTvLoggedInAs.setText(Html.fromHtml(String.format(getString(R.string.act__main__tv_logged_in), user.getScreenName())));
        } else {
            //noinspection deprecation
            mTvLoggedInAs.setText(Html.fromHtml(
                    String.format(getString(R.string.act__main__tv_logged_in_auto), Long.toString(user.getId()))));
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    @Override
    public void onCommWaitDialogCancelled() {
        getRes().abortLogin();
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            getRes().onConnectivityChange();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityResult = new ActivityResult(requestCode, resultCode, data);
    }
}
