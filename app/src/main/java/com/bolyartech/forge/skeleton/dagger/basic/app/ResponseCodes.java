package com.bolyartech.forge.skeleton.dagger.basic.app;

/**
 * Created by ogre on 2015-11-15 13:17
 */
public class ResponseCodes {
    public enum Oks {
        OK(1), // used as general code that indicates success
        REGISTER_AUTO_OK(2),
        REGISTER_OK(3),
        LOGIN_OK(4),
        SCREEN_NAME_OK(5),
        LOGOUT_OK(6),
        GCM_TOKEN_OK(50),;


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
        ERROR(-1), // used as general error when we cant/dont want to specify more details
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
        UUID_ALREADY_EXIST(-10),
        INVALID_USERNAME(-11),
        INVALID_PASSWORD(-12),
        UUID_MISMATCH(-13),


        /**
         * Login related codes
         */
        MALFORMED_LOGIN(-14), // when username or password or both are missing from the POST
        INVALID_LOGIN(-15), // user + password does not match valid account
        INVALID_LOGIN_SSL(-16),
        NOT_LOGGED_IN(-17); // not logged in


        private final int code;


        Errors(int code) {
            if (code < 0) {
                this.code = code;
            } else {
                throw new IllegalArgumentException("Code must be negative");
            }
        }


        public int getCode() {
            return code;
        }
    }
}
