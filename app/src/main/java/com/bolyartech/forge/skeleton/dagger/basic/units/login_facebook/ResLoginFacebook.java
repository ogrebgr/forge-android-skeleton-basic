package com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;


public interface ResLoginFacebook extends OperationResidentComponent {
    void checkFbLogin(String token);
}
