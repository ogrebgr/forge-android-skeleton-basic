package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;


public class FakeAppPrefs implements AppPrefs {
    private String mGcmToken;
    private boolean mGcmTokenSent;
    private LoginMethod mLastSuccessfulLoginMethod;
    private LoginMethod mSelectedLoginMethod;
    private boolean mAskedToShareOnFacebook;


    public FakeAppPrefs() {
    }


    public FakeAppPrefs(String gcmToken,
                        boolean gcmTokenSent,
                        LoginMethod lastSuccessfulLoginMethod,
                        LoginMethod selectedLoginMethod,
                        boolean askedToShareOnFacebook) {
        mGcmToken = gcmToken;
        mGcmTokenSent = gcmTokenSent;
        mLastSuccessfulLoginMethod = lastSuccessfulLoginMethod;
        mSelectedLoginMethod = selectedLoginMethod;
        mAskedToShareOnFacebook = askedToShareOnFacebook;
    }


    @Override
    public String getGcmToken() {
        return mGcmToken;
    }


    @Override
    public void setGcmToken(String gcmToken) {
        mGcmToken = gcmToken;
    }


    @Override
    public boolean isGcmTokenUpdatedOnServer() {
        return mGcmTokenSent;
    }


    @Override
    public void setGcmTokenUpdatedOnServer(boolean gcmTokenSent) {
        mGcmTokenSent = gcmTokenSent;
    }


    @Override
    public void save() {
        // empty
    }


    @Override
    public boolean getAskedToShareOnFacebook() {
        return mAskedToShareOnFacebook;
    }


    @Override
    public void setAskedToShareOnFacebook(boolean askedToShareOnFacebook) {
        mAskedToShareOnFacebook = askedToShareOnFacebook;
    }


    @Override
    public LoginMethod getLastSuccessfulLoginMethod() {
        return mLastSuccessfulLoginMethod;
    }


    @Override
    public void setLastSuccessfulLoginMethod(LoginMethod lastSuccessfulLoginMethod) {
        mLastSuccessfulLoginMethod = lastSuccessfulLoginMethod;
    }


    @Override
    public LoginMethod getSelectedLoginMethod() {
        return mSelectedLoginMethod;
    }


    @Override
    public void setSelectedLoginMethod(LoginMethod selectedLoginMethod) {
        mSelectedLoginMethod = selectedLoginMethod;
    }
}
