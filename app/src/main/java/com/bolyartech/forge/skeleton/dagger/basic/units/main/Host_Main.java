package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.mvp.Host;


public interface Host_Main extends Host {
    void startLoginActivity();
    void startRegisterActivity();

    void screenModeLoggedIn();

    void showCommWaitDialog();
    void hideCommWaitDialog();

    void showCommProblemDialog();

    void showLoggingInDialog();
    void hideLoggingInDialog();

    void showInvalidAutologinDialog();
    void showUpgradeNeededDialog();
}
