package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.mvp.Host;


public interface Host_Main extends Host {
    void startLoginActivity();
    void showCommWaitDialog();
    void screenModeLoggedIn();

    void hideCommWaitDialog();
    void showCommProblemDialog();

    void showLoggingInDialog();
    void hideLoggingInDialog();
}
