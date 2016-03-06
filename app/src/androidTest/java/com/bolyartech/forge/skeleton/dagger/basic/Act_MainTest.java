package com.bolyartech.forge.skeleton.dagger.basic;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bolyartech.forge.skeleton.dagger.basic.app.MyAppUnitManager;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DaggerMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DefaultMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitManagerDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;
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
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class Act_MainTest {
    @Rule
    public ActivityTestRule<Act_Main> mActivityRule = new ActivityTestRule<Act_Main>(
            Act_Main.class) {


        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();


            HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(DefaultMyAppDaggerComponent.createOkHttpClient(app));


            MyAppUnitManager myAppUnitManager = new MyAppUnitManager();


            MyAppDaggerComponent inj = DaggerMyAppDaggerComponent.builder().
                    myAppDaggerModule(DefaultMyAppDaggerComponent.createMyAppDaggerModule(app)).
                    appInfoDaggerModule(DefaultMyAppDaggerComponent.createAppInfoDaggerModule(app)).
                    exchangeDaggerModule(DefaultMyAppDaggerComponent.createExchangeDaggerModule(myAppUnitManager, app)).
                    httpsDaggerModule(httpsDaggerModule).
                    unitManagerDaggerModule(new UnitManagerDaggerModule(myAppUnitManager)).
                    build();

            DependencyInjector.init(inj);
            DependencyInjector.getInstance().inject(app);

            Espresso.registerIdlingResources(app.getForgeAndroidTaskExecutor());
        }
    };


    @Test
    public void test() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());

        onView(withText("Logout")).perform(click());

        onView(withId(R.id.btn_login))
                .check(matches(isDisplayed()));
    }


    @After
    public void tearDown() {
        MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        Espresso.unregisterIdlingResources(app.getForgeAndroidTaskExecutor());
    }
}
