package com.bolyartech.forge.skeleton.dagger.basic.app;

public class CurrentUser {
    private final long mId;
    private final String mScreenNameChosen;
    private final String mScreenNameDefault;


    public CurrentUser(long id, String screenNameChosen, String screenNameDefault) {
        mId = id;
        mScreenNameChosen = screenNameChosen;
        mScreenNameDefault = screenNameDefault;
    }


    public long getId() {
        return mId;
    }


    public String getScreenNameChosen() {
        return mScreenNameChosen;
    }


    public String getScreenNameDefault() {
        return mScreenNameDefault;
    }


    public boolean hasChosenScreenName() {
        return mScreenNameChosen != null;
    }

}
