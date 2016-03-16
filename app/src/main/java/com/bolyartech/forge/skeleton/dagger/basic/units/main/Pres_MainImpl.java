package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;

import com.bolyartech.forge.android.app_unit.ActivityResultContainer;
import com.bolyartech.forge.android.mvp.PresenterImpl;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;


public class Pres_MainImpl extends PresenterImpl implements Pres_Main {
    public Pres_MainImpl(Mod_Main resident, View_Main view, Host_Main host) {
        super(resident, view, host);
    }


    @Override
    protected Mod_Main getModel() {
        return (Mod_Main) super.getModel();
    }


    @Override
    protected View_Main getView() {
        return (View_Main) super.getView();
    }


    @Override
    protected Host_Main getHost() {
        return (Host_Main) super.getHost();
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
        if (mLoginPrefs.isManualRegistration()) {
            Intent intent = new Intent(Act_Main.this, Act_Login.class);
            startActivity(intent);

        } else {
            getModel().login();
        }
    }


    @Override
    public void onRegisterButtonClicked() {

    }
}
