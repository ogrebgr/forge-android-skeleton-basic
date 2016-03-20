package com.bolyartech.forge.skeleton.dagger.basic.app;

public interface AppConfiguration {
    String getAppVersion();

    AppPrefs getAppPrefs();

    LoginPrefs getLoginPrefs();

    boolean shallAutoregister();
    boolean shallUseGcm();

}
