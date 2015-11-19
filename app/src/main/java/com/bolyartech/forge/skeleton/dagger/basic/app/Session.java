package com.bolyartech.forge.skeleton.dagger.basic.app;


public interface Session {
    boolean isLoggedIn();

    void setIsLoggedIn(boolean isLoggedIn);

    /**
     * @param ttl seconds
     */
    void setSessionTTl(int ttl);

    /**
     * mark session as last active current time
     */
    void prolong();

    void logout();
}
