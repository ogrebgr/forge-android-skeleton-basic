package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.skeleton.dagger.basic.app.MyApp;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.Act_Main;

import javax.inject.Singleton;

import dagger.Component;


/**
 * Created by ogre on 2015-10-04
 */

@Component(modules = {MyAppDaggerModule.class,
        AppInfoDaggerModule.class,
        SessionDaggerModule.class,
        UnitManagerDaggerModule.class,
        NetworkInfoProviderDaggerModule.class,
        ExchangeDaggerModule.class,
})
@Singleton
public interface MyAppDaggerComponent {
    void inject(MyApp app);

    void inject(Act_Main act);

//    void inject(Act_Login act);
}
