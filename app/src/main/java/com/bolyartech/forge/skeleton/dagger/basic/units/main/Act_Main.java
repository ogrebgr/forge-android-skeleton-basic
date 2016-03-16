package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bolyartech.forge.android.app_unit.ResidentComponent;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.Df_CommWait;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class Act_Main extends SessionActivity implements DoesLogin, Df_CommWait.Listener,  Host_Main {
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


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return ff
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

ff

        return super.onOptionsItemSelected(item);
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


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mConnectivityChangeReceiver);
    }


    @Override
    public void onCommWaitDialogCancelled() {
        mResident.abortLogin();
    }


    @Override
    public void exitRequest() {
        finish();
    }


    @Override
    public void startLoginActivity() {
        Intent intent = new Intent(Act_Main.this, Act_Login.class);
        startActivity(intent);
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
