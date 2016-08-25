package com.bolyartech.forge.skeleton.dagger.basic.app;

public class CurrentUser {
    private final long mId;
    private final String mScreenName;


    public CurrentUser(long id, String screenName) {
        mId = id;
        mScreenName = screenName;
    }


    public long getId() {
        return mId;
    }


    public String getScreenName() {
        return mScreenName;
    }
}
