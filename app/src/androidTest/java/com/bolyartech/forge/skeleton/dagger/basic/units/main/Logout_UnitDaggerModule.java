package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitDaggerModule;

import dagger.Module;
import dagger.Provides;


@Module
public class Logout_UnitDaggerModule extends UnitDaggerModule {
    @Provides
    ResMain providesResMain() {

        return new Logout_UnitDaggerModule.ResMainLogoutTest();
    }


    private static class ResMainLogoutTest extends AbstractMultiOperationResidentComponent<ResMain.Operation>
            implements ResMain {


        @Override
        public void autoLoginIfNeeded() {
            switchToBusyState(Operation.LOGIN);
        }


        @Override
        public void login() {
            // empty
        }


        @Override
        public void abortLogin() {

        }


        @Override
        public void logout() {

        }


        @Override
        public int getLoginError() {
            return -1;
        }


        @Override
        public AutoregisteringError getAutoregisteringError() {
            return null;
        }


        @Override
        public CurrentUser getCurrentUser() {
            return null;
        }
    }
}

