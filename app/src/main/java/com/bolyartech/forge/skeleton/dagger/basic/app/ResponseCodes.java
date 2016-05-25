package com.bolyartech.forge.skeleton.dagger.basic.app;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by ogre on 2015-11-15 13:17
 */
public class ResponseCodes {
    public enum Oks {
        OK(1); // used as general mCode that indicates success


        private final int code;


        Oks(int code) {
            if (code > 0) {
                this.code = code;
            } else {
                throw new IllegalArgumentException("Code must be positive");
            }
        }

        public int getCode() {
            return code;
        }
    }


    public enum Errors {
        ERROR(-1), // used as general error when we cant/don't want to specify more details
        MISSING_PARAMETERS(-2), // missing required parameters
        REQUIRES_HTTPS(-3), // HTTPS protocol must be used
        INVALID_PARAMETER_VALUE(-4), // parameter value is invalid. For example: string is passed where int is expected
        INTERNAL_SERVER_ERROR(-5), // some serious problem occurred on the server
        UPGRADE_NEEDED(-6), // client version is too old and unsupported


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
        MALFORMED_LOGIN(-12), // when username or password or both are missing from the POST
        INVALID_LOGIN(-13), // user + password does not match valid account
        NOT_LOGGED_IN(-14), // not logged in

        NO_ENOUGH_PRIVILEGES(-15),

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
