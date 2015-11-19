package com.bolyartech.forge.skeleton.dagger.basic.app;

public interface LoginPrefs {
    String getUuidString();

    void setUuidString(String uuidString);

    String getUsername();

    void setUsername(String username);

    String getPassword();

    void setPassword(String password);

    void save();

    boolean hasLoginCredentials();

    boolean isManualRegistration();

    void setManualRegistration(boolean val);

    String getPublicName();

    void setPublicName(String publicName);
}
