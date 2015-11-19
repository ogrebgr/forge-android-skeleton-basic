package com.bolyartech.forge.skeleton.dagger.basic.misc;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by ogre on 2015-09-26
 */
public enum LoginMethod {
    APP(0),
    GOOGLE(1),
    FACEBOOK(2);

    private static final Map<Integer, LoginMethod> typesByValue = new HashMap<>();

    static {
        for (LoginMethod type : LoginMethod.values()) {
            typesByValue.put(type.getCode(), type);
        }
    }

    private final int mCode;


    LoginMethod(int code) {
        mCode = code;
    }


    public static LoginMethod fromInt(int code) {
        LoginMethod ret = typesByValue.get(code);
        if (ret != null) {
            return ret;
        } else {
            return null;
        }

    }


    public int getCode() {
        return mCode;
    }
}
