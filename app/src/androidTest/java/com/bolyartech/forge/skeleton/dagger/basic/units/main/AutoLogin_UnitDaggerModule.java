package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.android.app_unit.UnitActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitDaggerModule;

import dagger.Module;
import dagger.Provides;


@Module
public class AutoLogin_UnitDaggerModule extends UnitDaggerModule {
    @Provides
    ResMain providesResMain() {

        return new ResMainAutoLoginTest();
    }


    private static class ResMainAutoLoginTest extends AbstractMultiOperationResidentComponent<ResMain.Operation>
            implements ResMain {


        @Override
        public void autoLoginIfNeeded() {
            switchToCompletedStateSuccess();
        }


        @Override
        public void login() {

        }


        @Override
        public void abortLogin() {

        }


        @Override
        public void logout() {

        }


        @Override
        public void onConnectivityChange() {

        }


        @Override
        public LoginError getLoginError() {
            return null;
        }


        @Override
        public AutoregisteringError getAutoregisteringError() {
            return null;
        }
    }
}
