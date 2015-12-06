package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;
import com.squareup.otto.Subscribe;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class Act_Main extends SessionActivity implements DoesLogin {
    private static final int ACT_LOGIN = 1;

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());


    @Inject
    Provider<Res_MainImpl> mRes_MainImplProvider;

    private Res_Main mResident;

    private ConnectivityChangeReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();

    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;

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

        getDependencyInjector().inject(this);

        initViews();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act__main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.ab_logout) {
            mResident.logout();
        } else if (id == R.id.ab_select_login) {
            Intent intent = new Intent(this, Act_Login.class);
            startActivityForResult(intent, ACT_LOGIN);
        }


        return super.onOptionsItemSelected(item);
    }

    private void initViews() {
        View view = getWindow().getDecorView();

        mViewNoInet = ViewUtils.findViewX(view, R.id.v_no_inet);
        mViewNotLoggedIn = ViewUtils.findViewX(view, R.id.v_not_logged_in);
        mViewLoggedIn = ViewUtils.findViewX(view, R.id.v_logged_in);

        ViewUtils.initButton(view, R.id.btn_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mResident.login();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        mResident = (Res_Main) getResidentComponent();

        handleState(mResident.getState());
    }


    private synchronized void handleState(Res_Main.State state) {
        mLogger.debug("State: {}", state);
        switch (state) {
            case NO_INET:
                screenModeNoInet();
                break;
            case NOT_LOGGED_IN:
                screenModeNotLoggedIn();
                break;
            case IDLE:
                screenModeLoggedIn();
                break;
            case AUTO_REGISTERING:
                MyAppDialogs.showCommWaitDialog(getFragmentManager());
                break;
            case REGISTER_AUTO_FAIL:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                break;
            case SESSION_STARTED_OK:
                MyAppDialogs.hideCommWaitDialog(getFragmentManager());
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                screenModeLoggedIn();
                break;
            case SESSION_START_FAIL:
                // TODO implement
                throw new AssertionError("Not implemented");
            case LOGGING_IN:
                MyAppDialogs.showLoggingInDialog(getFragmentManager());
                break;
            case LOGIN_FAIL:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showCommProblemDialog(getFragmentManager());
                screenModeNotLoggedIn();
                break;
            case LOGIN_INVALID:
                MyAppDialogs.hideLoggingInDialog(getFragmentManager());
                MyAppDialogs.showInvalidAutologinDialog(getFragmentManager());
                screenModeNotLoggedIn();
        }
        throw new IllegalArgumentException("aaaaaa");
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
    }


    private void screenModeLoggedIn() {
        mViewNoInet.setVisibility(View.GONE);
        mViewNotLoggedIn.setVisibility(View.GONE);
        mViewLoggedIn.setVisibility(View.VISIBLE);
    }


    @Subscribe
    public void onEv_StateChanged(Ev_StateChanged ev) {
        handleState(mResident.getState());
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    private class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mResident != null) {
                mResident.onConnectivityChange();
            }
        }
    }
}
