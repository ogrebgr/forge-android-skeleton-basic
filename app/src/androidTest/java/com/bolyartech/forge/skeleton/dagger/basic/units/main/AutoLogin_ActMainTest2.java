package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DefaultMyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.DependencyInjector;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.HttpsDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.utils.ElapsedTimeIdlingResource;
import com.bolyartech.forge.skeleton.dagger.basic.utils.MyTestApp;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;


@RunWith(AndroidJUnit4.class)
public class AutoLogin_ActMainTest2 {

    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass()
            .getSimpleName());

    @Rule
    public ActivityTestRule<ActMain> mActivityRule = new ActivityTestRule<ActMain>(
            ActMain.class) {


        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
            DependencyInjector.reset();
            app.reset();

            HttpsDaggerModule httpsDaggerModule = new HttpsDaggerModule(DefaultMyAppDaggerComponent.createOkHttpClient(app, true));


            AutoLogin_Component inj = DaggerAutoLogin_Component.builder().
                    myAppDaggerModule(DefaultMyAppDaggerComponent.createMyAppDaggerModule(app)).
                    appInfoDaggerModule(DefaultMyAppDaggerComponent.createAppInfoDaggerModule(app)).
                    autoLogin_ExchangeDaggerModule(new AutoLogin_ExchangeDaggerModule()).
                    httpsDaggerModule(httpsDaggerModule).
                    unitDaggerModule(new AutoLogin_UnitDaggerModule()).
                    build();

            DependencyInjector.init(inj);
            DependencyInjector.getInstance().inject(app);

            Espresso.registerIdlingResources(app.getForgeAndroidTaskExecutor());

            app.onStart();
        }
    };


    @Test
    public void testAutoLogin() {
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(500);
        Espresso.registerIdlingResources(idlingResource);
        onView(ViewMatchers.withId(R.id.tv_logged_in_as)).check(matches(isDisplayed()));
        Espresso.unregisterIdlingResources(idlingResource);
    }


    @After
    public void tearDown() {
        MyTestApp app = (MyTestApp) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        Espresso.unregisterIdlingResources(app.getForgeAndroidTaskExecutor());
    }
}
