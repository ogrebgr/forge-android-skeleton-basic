package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.ResponseCode;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility class that contains authentication response codes
 */
public class AuthenticationResponseCodes {
    /**
     * Disabling instantiation
     */
    private AuthenticationResponseCodes() {
        throw new AssertionError("Non-instantiable utility class");
    }


    /**
     * Utility class that contains authentication error codes
     */
    public static class Errors {
        /**
         * Disabling instantiation
         */
        private Errors() {
            throw new AssertionError("Non-instantiable utility class");
        }

        /**
         * Registration of a new user is refused for some reason
         */
        public static final int REGISTRATION_REFUSED = -7;
        /**
         * Username already taken
         */
        public static final int USERNAME_EXISTS = -8;
        /**
         * Password is too short or too weak
         */
        public static final int UNACCEPTABLE_PASSWORD = -9;
        /**
         * Username contains illegal characters or has invalid length
         */
        public static final int INVALID_USERNAME = -10;
        /**
         * Password contain illegal characters or has invalid length
         */
        public static final int INVALID_PASSWORD = -11;


        /**
         * Invalid login
         */
        public static final int INVALID_LOGIN = -12;
        /**
         * Not logged in
         */
        public static final int NOT_LOGGED_IN = -13;
        /**
         * No enough privileges for that operation
         */
        public static final int NO_ENOUGH_PRIVILEGES = -14;


        /**
         * Screen name contains invalid characters ot has invalid length
         */
        public static final int INVALID_SCREEN_NAME = -50;
        /**
         * Screen name is already taken
         */
        public static final int SCREEN_NAME_EXISTS = -51;
        /**
         * Screen name change is disabled
         */
        public static final int SCREEN_NAME_CHANGE_NOT_SUPPORTED = -52;
    }
}
