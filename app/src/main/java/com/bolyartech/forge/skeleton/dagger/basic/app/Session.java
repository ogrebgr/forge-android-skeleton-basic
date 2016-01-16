package com.bolyartech.forge.skeleton.dagger.basic.app;


public interface Session {
    boolean isLoggedIn();

    /**
     * @param ttl seconds
     */
    void startSession(int ttl);

    /**
     * mark session as last active current time
     */
    void prolong();

    void logout();
}
