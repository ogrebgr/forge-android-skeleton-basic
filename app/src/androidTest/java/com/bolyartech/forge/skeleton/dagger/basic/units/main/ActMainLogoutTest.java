package com.bolyartech.forge.skeleton.dagger.basic.units.main;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DaggerMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DefaultMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitDaggerModule;
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
public class ActMainLogoutTest {
    @Rule
    public ActivityTestRule<ActMain> mActivityRule = new ActivityTestRule<ActMain>(
            ActMain.class) {


        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
            DependencyInjector.reset();
            app.reset();

            HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(DefaultMyAppDaggerComponent.createOkHttpClient(app, true));


            MyAppDaggerComponent inj = DaggerMyAppDaggerComponent.builder().
                    myAppDaggerModule(DefaultMyAppDaggerComponent.createMyAppDaggerModule(app)).
                    appInfoDaggerModule(DefaultMyAppDaggerComponent.createAppInfoDaggerModule(app)).
                    exchangeDaggerModule(DefaultMyAppDaggerComponent.createExchangeDaggerModule(app)).
                    httpsDaggerModule(httpsDaggerModule).
                    unitDaggerModule(new UnitDaggerModule()).
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
}
