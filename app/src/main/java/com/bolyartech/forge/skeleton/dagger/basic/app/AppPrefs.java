package com.bolyartech.forge.skeleton.dagger.basic.app;


import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;


public interface AppPrefs {
    String getGcmToken();

    void setGcmToken(String token);

    boolean isGcmTokenUpdatedOnServer();

    void setGcmTokenUpdatedOnServer(boolean sent);

    void save();

    boolean getAskedToShareOnFacebook();

    void setAskedToShareOnFacebook(boolean asked);

    LoginMethod getLastSuccessfulLoginMethod();

    void setLastSuccessfulLoginMethod(LoginMethod method);

    LoginMethod getSelectedLoginMethod();

    void setSelectedLoginMethod(LoginMethod method);
}
