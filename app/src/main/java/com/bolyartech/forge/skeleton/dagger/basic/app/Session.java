package com.bolyartech.forge.skeleton.dagger.basic.app;


import android.support.annotation.NonNull;

import com.bolyartech.forge.base.misc.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;


public interface Session {
    boolean isLoggedIn();

    /**
     * @param ttl seconds
     */
    void startSession(int ttl, Info info);

    Info getInfo();

    /**
     * mark session as last active current time
     */
    void prolong();

    void logout();


    class Info {
        private final long mUserId;
        private String mScreenName;


        public Info(long userId, String screenName) {
            mUserId = userId;
            mScreenName = screenName;
        }


        public long getUserId() {
            return mUserId;
        }


        public void setScreenName(String screenName) {
            mScreenName = screenName;
        }


        public String getScreenName() {
            return mScreenName;
        }


        public boolean hasScreenName() {
            return StringUtils.isNotEmpty(mScreenName);
        }


        public static Info fromJson(@NonNull JSONObject jobj) throws JSONException {
            return new Info(jobj.getLong("user_id"),
                            jobj.getString("screen_name")
                            );
        }
    }
}
