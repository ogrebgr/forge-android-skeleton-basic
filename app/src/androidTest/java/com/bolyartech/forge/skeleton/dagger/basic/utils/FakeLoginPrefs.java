package com.bolyartech.forge.skeleton.dagger.basic.utils;

import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;


public class FakeLoginPrefs implements LoginPrefs {
    private String mUsername;
    private String mPassword;
    private String mPublicName;

    private boolean mManualReg;


    public FakeLoginPrefs() {
    }


    public FakeLoginPrefs(String username, String password, String publicName, boolean manualReg) {
        mUsername = username;
        mPassword = password;
        mPublicName = publicName;
        mManualReg = manualReg;
    }


    @Override
    public String getUsername() {
        return mUsername;
    }


    @Override
    public void setUsername(String username) {
        mUsername = username;
    }


    @Override
    public String getPassword() {
        return mPassword;
    }


    @Override
    public void setPassword(String password) {
        mPassword = password;
    }


    @Override
    public void save() {
        // empty
    }


    @Override
    public boolean hasLoginCredentials() {
        return StringUtils.isNotEmpty(mUsername) && StringUtils.isNotEmpty(mPassword);
    }


    @Override
    public boolean isManualRegistration() {
        return mManualReg;
    }


    @Override
    public void setManualRegistration(boolean val) {
        mManualReg = val;
    }


    @Override
    public String getPublicName() {
        return mPublicName;
    }


    @Override
    public void setPublicName(String publicName) {
        mPublicName = publicName;
    }
}
