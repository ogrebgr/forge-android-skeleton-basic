package com.bolyartech.forge.skeleton.dagger.basic.units.select_login;

import com.bolyartech.forge.android.app_unit.MultiOperationResidentToActivity;


public interface RtaSelectLogin extends MultiOperationResidentToActivity<ResSelectLogin.Operation> {
    void checkFbLogin(String token, String facebookUserId);

    void checkGoogleLogin(String token);

}
