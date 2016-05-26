package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.os.SystemClock;


import com.bolyartech.forge.base.exchange.ExchangeOutcome;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;

import javax.inject.Inject;


public class SessionImpl implements Session {
    boolean mIsLoggedIn;


    private int mSessionTtl;
    private long mLastSessionProlong; //in seconds
    private Info mInfo;


    @Inject
    public SessionImpl() {
        super();
    }


    public static boolean isNeedLogin(ExchangeOutcome<ForgeExchangeResult> out) {
        boolean ret = false;

        if (!out.isError()) {
            ForgeExchangeResult rez = out.getResult();
            int code = rez.getCode();
            if (code == BasicResponseCodes.Errors.NOT_LOGGED_IN.getCode()) {
                ret = true;
            }
        }

        return ret;
    }


    @Override
    public boolean isLoggedIn() {
        checkSessionExpired();
        return mIsLoggedIn;
    }


    @Override
    public void startSession(int ttl, Info info) {
        mSessionTtl = ttl;
        mInfo = info;
        setIsLoggedIn(true);
        prolong();
    }


    @Override
    public Info getInfo() {
        return mInfo;
    }


    private void checkSessionExpired() {
        if (mLastSessionProlong + mSessionTtl < (SystemClock.elapsedRealtime() / 1000)) {
            mIsLoggedIn = false;
        }
    }


    private void setIsLoggedIn(boolean isLoggedIn) {
        mIsLoggedIn = isLoggedIn;
    }


    @Override
    public void prolong() {
        if (mIsLoggedIn) {
            mLastSessionProlong = SystemClock.elapsedRealtime() / 1000;
        }
    }


    @Override
    public void logout() {
        setIsLoggedIn(false);
    }
}
