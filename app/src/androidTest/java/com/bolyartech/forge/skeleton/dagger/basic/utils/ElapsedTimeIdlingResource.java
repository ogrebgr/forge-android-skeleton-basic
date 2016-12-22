package com.bolyartech.forge.skeleton.dagger.basic.utils;

import android.support.test.espresso.IdlingResource;


public class ElapsedTimeIdlingResource implements IdlingResource {
    private final long mWaitingTime;
    private long mStartTime;
    private ResourceCallback mResourceCallback;


    public ElapsedTimeIdlingResource(long waitingTime) {
        this.mWaitingTime = waitingTime;
    }


    public void startWaiting() {
        mStartTime = System.currentTimeMillis();
    }


    @Override
    public String getName() {
        return ElapsedTimeIdlingResource.class.getName() + ":" + mWaitingTime;
    }


    @Override
    public boolean isIdleNow() {
        long elapsed = System.currentTimeMillis() - mStartTime;
        boolean idle = (elapsed >= mWaitingTime);
        if (idle) {
            mResourceCallback.onTransitionToIdle();
        }
        return idle;
    }


    @Override
    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.mResourceCallback = resourceCallback;
    }
}