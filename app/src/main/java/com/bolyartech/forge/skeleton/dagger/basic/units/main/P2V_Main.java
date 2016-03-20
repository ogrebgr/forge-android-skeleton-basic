package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.mvp.P2V;


public interface P2V_Main extends P2V {
    void screenModeLoggedIn();
    void screenModeNotLoggedIn();
    void screenModeNoInet();
    void invalidateOptionsMenu();
    void setRegisterButtonVisible(boolean isVisible);
}
