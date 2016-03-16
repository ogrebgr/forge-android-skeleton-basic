package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.android.mvp.ActivityView;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.Act_Login;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.Act_Register;


public class View_MainImpl implements ActivityView {
    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mTvLoggedInAs;


    @Override
    public void onCreate(View rootView) {
        mViewNoInet = ViewUtils.findViewX(rootView, R.id.v_no_inet);
        mViewNotLoggedIn = ViewUtils.findViewX(rootView, R.id.v_not_logged_in);
        mViewLoggedIn = ViewUtils.findViewX(rootView, R.id.v_logged_in);

        mTvLoggedInAs = ViewUtils.findTextViewX(rootView, R.id.tv_logged_in_as);

        mBtnLogin = ViewUtils.initButton(rootView, R.id.btn_login, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                f
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
}
