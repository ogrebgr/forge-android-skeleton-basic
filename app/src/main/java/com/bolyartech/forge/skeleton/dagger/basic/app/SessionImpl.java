package com.bolyartech.forge.skeleton.dagger.basic.app;

import android.os.SystemClock;


import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeResult;

import javax.inject.Inject;


public class SessionImpl implements Session {
    boolean mIsLoggedIn;


    private int mSessionTtl;
    private long mLastSessionProlong; //in seconds


    @Inject
    public SessionImpl() {
        super();
    }


    public static boolean isNeedLogin(ExchangeOutcome<ForgeExchangeResult> out) {
        boolean ret = false;

        if (!out.isError()) {
            ForgeExchangeResult rez = out.getResult();
            int code = rez.getCode();
            if (code == ResponseCodes.Errors.NOT_LOGGED_IN.getCode()) {
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


    private void checkSessionExpired() {
        if (mLastSessionProlong + mSessionTtl < (SystemClock.elapsedRealtime() / 1000)) {
            mIsLoggedIn = false;
        }
    }


    public void setIsLoggedIn(boolean isLoggedIn) {
        mIsLoggedIn = isLoggedIn;
    }


    @Override
    public void setSessionTTl(int ttl) {
        mSessionTtl = ttl;
    }


    @Override
    public void prolong() {
        mLastSessionProlong = SystemClock.elapsedRealtime() / 1000;
    }


    @Override
    public void logout() {
        mIsLoggedIn = false;
    }
}
