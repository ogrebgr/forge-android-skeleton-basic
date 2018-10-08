package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bolyartech.forge.android.misc.ActivityResult;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.RctSessionActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommProblem;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfCommWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfLoggingIn;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.PerformsLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ActLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.LoginTask;
import com.bolyartech.forge.skeleton.dagger.basic.units.rc_test.ActRcTest;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ActRegister;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ActScreenName;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import dagger.Lazy;


public class ActMain extends RctSessionActivity<ResMain> implements PerformsLogin, DfCommWait.Listener,
        DfCommProblem.Listener, DfLoggingIn.Listener {


    private static final int ACT_SELECT_LOGIN = 1;
    private static final int ACT_REGISTER = 2;
    private static final int ACT_LOGIN_FACEBOOK = 3;
    private static final int ACT_LOGIN_GOOGLE = 4;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    NetworkInfoProvider mNetworkInfoProvider;

    @Inject
    LoginPrefs mLoginPrefs;

    @Inject
    AppConfiguration mAppConfiguration;

    @Inject
    Lazy<ResMain> mResMainLazy;

    private View mViewNoInet;
    private View mViewNotLoggedIn;
    private View mViewLoggedIn;
    private Button mBtnRegister;
    private Button mBtnLogin;
    private TextView mTvLoggedInAs;

    private boolean tryAutologin;


    @NonNull
    @Override
    public ResMain createResidentComponent() {
        return mResMainLazy.get();
    }


    @Override
    public void onCommProblemClosed() {
        // if we did not managed to autoregister
        if (!mLoginPrefs.hasLoginCredentials()) {
            // exit app
            finish();
        }
    }


    @Override
    public void onLoggingInDialogCancelled() {
        if (getRes().isBusy()) {
            getRes().abort();
            screenModeNotLoggedIn();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.act__main, menu);
        if (getSession().isLoggedIn()) {
            CurrentUser user = getRes().getCurrentUser();
            if (!user.hasScreenName()) {
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
            invalidateOptionsMenu();
            screenModeNotLoggedIn();
        } else if (id == R.id.ab_screen_name) {
            Intent intent = new Intent(ActMain.this, ActScreenName.class);
            startActivity(intent);
        } else if (id == R.id.ab_registration || id == R.id.ab_full_registration) {
            Intent intent = new Intent(ActMain.this, ActRegister.class);
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCommWaitDialogCancelled() {
        getRes().abort();
    }


    @Override
    public void handleResidentEndedState() {
        if (getRes().getCurrentTask() != null) {
            switch (getRes().getCurrentTask().getId()) {
                case AutoregisterTask.TASK_ID:
                    MyAppDialogs.hideCommWaitDialog(getSupportFragmentManager());
                    if (getRes().getCurrentTask().isSuccess()) {
                        screenModeLoggedIn();
                    } else {
                        switch (getRes().getAutoregisterError()) {
                            case BasicResponseCodes.Errors.UPGRADE_NEEDED:
                                MyAppDialogs.showUpgradeNeededDialog(getSupportFragmentManager());
                                break;
                            default:
                                MyAppDialogs.showCommProblemDialog(getSupportFragmentManager());
                                break;
                        }
                    }
                    break;
                case LoginTask.TASK_ID:
                    MyAppDialogs.hideLoggingInDialog(getSupportFragmentManager());
                    if (getRes().getCurrentTask().isSuccess()) {
                        screenModeLoggedIn();
                    } else {
                        switch (getRes().getLoginError()) {
                            case AuthenticationResponseCodes.Errors.INVALID_LOGIN:
                                screenModeNotLoggedIn();
                                MyAppDialogs.showInvalidAutologinDialog(getSupportFragmentManager());
                                break;
                            case BasicResponseCodes.Errors.UPGRADE_NEEDED:
                                MyAppDialogs.showUpgradeNeededDialog(getSupportFragmentManager());
                                break;
                            default:
                                MyAppDialogs.showCommProblemDialog(getSupportFragmentManager());
                                screenModeNotLoggedIn();
                                break;
                        }
                    }
                    break;
                case LogoutTask.TASK_ID:
                    tryAutologin = false;
                    screenModeNotLoggedIn();
                    break;
            }
        }
    }


    @Override
    public void handleResidentBusyState() {
        if (getRes().getCurrentTask() != null) {
            switch (getRes().getCurrentTask().getId()) {
                case AutoregisterTask.TASK_ID:
                    MyAppDialogs.showCommWaitDialog(getSupportFragmentManager());
                    break;
                case LoginTask.TASK_ID:
                    MyAppDialogs.showLoggingInDialog(getSupportFragmentManager());
                    break;
            }
        }
    }


    @Override
    public void handleResidentIdleState() {
        if (mNetworkInfoProvider.isConnected()) {
            if (getSession().isLoggedIn()) {
                screenModeLoggedIn();
            } else {
                if (tryAutologin) {
                    tryAutologin = false;

                    screenModeBlank();
                    getRes().autoLoginIfNeeded();
                }
            }
        } else {
            screenModeNoInet();
        }
    }


    @Override
    public void handleActivityResult(ActivityResult activityResult) {
        super.handleActivityResult(activityResult);

        if (activityResult.requestCode == ACT_REGISTER) {
            if (activityResult.resultCode == Activity.RESULT_OK) {
                invalidateOptionsMenu();
                screenModeLoggedIn();
            }
        } else if (activityResult.requestCode == ACT_SELECT_LOGIN) {
            if (activityResult.resultCode == Activity.RESULT_OK) {
                invalidateOptionsMenu();
                screenModeLoggedIn();
            }
        } else if (activityResult.requestCode == ACT_LOGIN_FACEBOOK) {
            if (activityResult.resultCode == Activity.RESULT_OK) {
                invalidateOptionsMenu();
                screenModeLoggedIn();
            }
        } else if (activityResult.requestCode == ACT_LOGIN_GOOGLE) {
            if (activityResult.resultCode == Activity.RESULT_OK) {
                invalidateOptionsMenu();
                screenModeLoggedIn();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act__main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // on start we will try to autologin once
        tryAutologin = true;

        initViews();
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
                if (mLoginPrefs.hasLoginCredentials()) {
                    getRes().login();
                }
            }
        });

        mBtnRegister = ViewUtils.initButton(view, R.id.btn_register, v -> {
            Intent intent = new Intent(ActMain.this, ActRegister.class);
            startActivityForResult(intent, ACT_REGISTER);
        });


        if (getResources().getBoolean(R.bool.app_conf__do_autoregister)) {
            mBtnRegister.setVisibility(View.GONE);
        }

        ViewUtils.initButton(view, R.id.btn_rctest, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActMain.this, ActRcTest.class);
                startActivity(intent);
            }
        });
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


    private void screenModeBlank() {
        mViewNoInet.setVisibility(View.GONE);
        mViewNotLoggedIn.setVisibility(View.GONE);
        mViewLoggedIn.setVisibility(View.GONE);

        mBtnLogin.setVisibility(View.GONE);
        mBtnRegister.setVisibility(View.GONE);
    }


    private void screenModeLoggedIn() {
        invalidateOptionsMenu();
        mViewNoInet.setVisibility(View.GONE);
        mViewNotLoggedIn.setVisibility(View.GONE);
        mViewLoggedIn.setVisibility(View.VISIBLE);

        mBtnLogin.setVisibility(View.GONE);
        mBtnRegister.setVisibility(View.GONE);

        CurrentUser user = getRes().getCurrentUser();
        if (mLoginPrefs.isManualRegistration()) {
            if (!user.hasScreenName()) {
                //noinspection deprecation
                mTvLoggedInAs.setText(Html.fromHtml(
                        String.format(getString(R.string.act__main__tv_logged_in),
                                user.getScreenName())
                ));
            } else {
                //noinspection deprecation
                mTvLoggedInAs.setText(Html.fromHtml(
                        String.format(getString(R.string.act__main__tv_logged_in_with_screen_name),
                                mLoginPrefs.getUsername(), user.getScreenName())
                ));
            }
        } else {
            if (!user.hasScreenName()) {
                //noinspection deprecation
                mTvLoggedInAs.setText(Html.fromHtml(
                        String.format(getString(R.string.act__main__tv_logged_in_default),
                                user.getId())
                ));
            } else {
                //noinspection deprecation
                mTvLoggedInAs.setText(Html.fromHtml(
                        String.format(getString(R.string.act__main__tv_logged_in_default_with_screen_name),
                                user.getId(), user.getScreenName())
                ));
            }
        }
    }

}
