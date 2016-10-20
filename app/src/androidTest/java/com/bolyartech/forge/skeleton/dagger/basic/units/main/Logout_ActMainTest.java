package com.bolyartech.forge.skeleton.dagger.basic.units.main;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bolyartech.forge.android.app_unit.AbstractMultiOperationResidentComponent;
import com.bolyartech.forge.base.misc.TimeProviderImpl;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.base.session.SessionImpl;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.AppInfoDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.utils.DaggerFakeComponent;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeAppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeComponent;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeExchangeDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeLoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeMyAppDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeSessionDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.utils.FakeUnitDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.utils.MyTestApp;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class Logout_ActMainTest {
    @Rule
    public ActivityTestRule<ActMain> mActivityRule = new ActivityTestRule<ActMain>(
            ActMain.class) {


        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
            DependencyInjector.reset();
            app.reset();


            FakeUnitDaggerModule fudm = new FakeUnitDaggerModule();
            Session session = new SessionImpl(new TimeProviderImpl());
            fudm.setResMain(new ResMainTest(session));

            FakeAppPrefs appPrefs = new FakeAppPrefs();
            FakeLoginPrefs loginPrefs = new FakeLoginPrefs();
            CurrentUserHolder currentUserHolder = new CurrentUserHolder();

            FakeComponent inj = DaggerFakeComponent.builder().
                    fakeMyAppDaggerModule(new FakeMyAppDaggerModule(app, appPrefs, loginPrefs, currentUserHolder)).
                    appInfoDaggerModule(new AppInfoDaggerModule("1")).
                    fakeSessionDaggerModule(new FakeSessionDaggerModule(session)).
                    fakeExchangeDaggerModule(new FakeExchangeDaggerModule("https://test.com")).
                    fakeUnitDaggerModule(fudm).
                    build();

            DependencyInjector.init(inj);
            DependencyInjector.getInstance().inject(app);

            Espresso.registerIdlingResources(app.getForgeAndroidTaskExecutor());

            app.onStart();
        }
    };


    @Test
    public void test() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());

        onView(withText("Logout")).perform(click());

        onView(ViewMatchers.withId(R.id.btn_login))
                .check(matches(isDisplayed()));
    }


    @After
    public void tearDown() {
        MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        Espresso.unregisterIdlingResources(app.getForgeAndroidTaskExecutor());
    }


    private static class ResMainTest extends AbstractMultiOperationResidentComponent<ResMain.Operation>
            implements ResMain {

        private final Session mSession;

        private CurrentUser mCurrentUser;
        private boolean mLoggedIn = false;


        public ResMainTest(Session session) {
            mSession = session;
        }


        @Override
        public void autoLoginIfNeeded() {
            if (!mLoggedIn) {
                mLoggedIn = true;
                mCurrentUser = new CurrentUser(1, "test");
                mSession.startSession(1000);
                switchToBusyState(Operation.LOGIN);
                switchToCompletedStateSuccess();
            }
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
            switchToBusyState(Operation.LOGOUT);
            switchToCompletedStateSuccess();
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


        @Override
        public CurrentUser getCurrentUser() {
            return mCurrentUser;
        }
    }
}
