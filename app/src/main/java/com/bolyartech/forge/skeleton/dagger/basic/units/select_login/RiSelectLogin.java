package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.MorcActivityInterface;


public interface RiSelectLogin extends MorcActivityInterface<ResSelectLogin.Operation> {
    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

}
