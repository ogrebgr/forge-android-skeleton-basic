package com.bolyartech.forge.skeleton.dagger.basic.app;


import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;


public interface AppPrefs {
    String getGcmToken();

    void setGcmToken(String token);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isGcmTokenUpdatedOnServer();

    void setGcmTokenUpdatedOnServer(boolean sent);

    long getUserId();

    void setUserId(long userId);

    void save();

    boolean getAskedToShareOnFacebook();

    void setAskedToShareOnFacebook(boolean asked);

    LoginMethod getLastSuccessfulLoginMethod();

    void setLastSuccessfulLoginMethod(LoginMethod method);

    LoginMethod getSelectedLoginMethod();

    void setSelectedLoginMethod(LoginMethod method);
}
