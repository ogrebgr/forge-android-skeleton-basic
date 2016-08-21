package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.ResponseCode;

import java.util.HashMap;
import java.util.Map;


public class AuthorizationResponseCodes {
    public enum Errors implements ResponseCode {
        /**
         * Registration related codes
         */
        REGISTRATION_REFUSED(-7),
        USERNAME_EXISTS(-8),
        PASSWORD_TOO_SHORT(-9),
        INVALID_USERNAME(-10),
        INVALID_PASSWORD(-11),


        /**
         * Login related codes
         */
        INVALID_LOGIN(-12), // user + password does not match valid account
        NOT_LOGGED_IN(-13), // not logged in

        NO_ENOUGH_PRIVILEGES(-14),

        INVALID_SCREEN_NAME(-50),
        SCREEN_NAME_EXISTS(-51),
        SCREEN_NAME_CHANGE_NOT_SUPPORTED(-52);



        private static final Map<Integer, Errors> mTypesByValue = new HashMap<>();

        static {
            for (Errors type : Errors.values()) {
                mTypesByValue.put(type.getCode(), type);
            }
        }


        private final int mCode;


        Errors(int code) {
            if (code < 0) {
                this.mCode = code;
            } else {
                throw new IllegalArgumentException("Code must be negative");
            }
        }


        public int getCode() {
            return mCode;
        }

        public static Errors fromInt(int code) {
            Errors ret = mTypesByValue.get(code);
            if (ret != null) {
                return ret;
            } else {
                return null;
            }
        }
    }

}
