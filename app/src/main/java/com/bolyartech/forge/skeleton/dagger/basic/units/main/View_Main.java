package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.android.mvp.ActivityView;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.Act_Register;


public class View_Main implements ActivityView, P2V_Main {
    private V2P_Main mV2p;


    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mTvLoggedInAs;


    void attachV2p(V2P_Main v2p) {
        mV2p = v2p;
    }


    @Override
    public void onCreate(View rootView) {
        mViewNoInet = ViewUtils.findViewX(rootView, R.id.v_no_inet);
        mViewNotLoggedIn = ViewUtils.findViewX(rootView, R.id.v_not_logged_in);
        mViewLoggedIn = ViewUtils.findViewX(rootView, R.id.v_logged_in);

        mTvLoggedInAs = ViewUtils.findTextViewX(rootView, R.id.tv_logged_in_as);

        mBtnLogin = ViewUtils.initButton(rootView, R.id.btn_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mV2p.onLoginButtonClicked();
            }
        });

        mBtnRegister = ViewUtils.initButton(rootView, R.id.btn_register, new View.OnClickListener() {
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
    public boolean onCreateOptionsMenu(MenuInflater inflater, Menu menu) {
        inflater.inflate(R.menu.act__main, menu);

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

    }


    @Override
    public void onDestroy() {
        mV2p = null;
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
}
