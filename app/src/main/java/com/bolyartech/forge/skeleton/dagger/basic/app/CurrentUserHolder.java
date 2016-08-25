package com.bolyartech.forge.skeleton.dagger.basic.app;

public class CurrentUserHolder {
    private CurrentUser mCurrentUser;


    public CurrentUser getCurrentUser() {
        return mCurrentUser;
    }


    public void setCurrentUser(CurrentUser currentUser) {
        mCurrentUser = currentUser;
    }
}
