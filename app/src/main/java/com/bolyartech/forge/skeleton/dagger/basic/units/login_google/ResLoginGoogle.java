package com.bolyartech.forge.skeleton.dagger.basic.units.login_google;

import com.bolyartech.forge.android.app_unit.OperationResidentComponent;


public interface ResLoginGoogle extends OperationResidentComponent {
    void checkGoogleLogin(String token);
}
