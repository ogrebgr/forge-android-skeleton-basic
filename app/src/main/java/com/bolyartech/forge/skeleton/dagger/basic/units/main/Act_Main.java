package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.bolyartech.forge.app_unit.ResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionActivityComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.DoesLogin;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Created by ogre on 2015-11-17 17:16
 */
public class Act_Main extends SessionActivityComponent implements DoesLogin {
    @Inject
    Provider<Res_MainImpl> mRes_MainImplProvider;

    private Res_Main mResident;

    private ConnectivityChangeReceiver mConnectivityChangeReceiver = new ConnectivityChangeReceiver();

    @Override
    public ResidentComponent createResidentComponent() {
        return mRes_MainImplProvider.get();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDependencyInjector().inject(this);
    }


    @Override
    public void onResume() {
        super.onResume();

        registerReceiver(mConnectivityChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));


        mResident = (Res_Main) getResidentComponent();

        handleState(mResident.getState());
    }


    private synchronized void handleState(Res_Main.State state) {
        switch (state) {
            case IDLE:
                break;

        }
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
