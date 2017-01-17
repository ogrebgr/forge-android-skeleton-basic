package com.bolyartech.forge.skeleton.dagger.basic.dagger;


import com.bolyartech.forge.skeleton.dagger.basic.app.App;
import com.bolyartech.forge.skeleton.dagger.basic.units.login.ActLogin;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_facebook.ActLoginFacebook;
import com.bolyartech.forge.skeleton.dagger.basic.units.login_google.ActLoginGoogle;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.ActMain;
import com.bolyartech.forge.skeleton.dagger.basic.units.register.ActRegister;
import com.bolyartech.forge.skeleton.dagger.basic.units.screen_name.ActScreenName;
import com.bolyartech.forge.skeleton.dagger.basic.units.select_login.ActSelectLogin;

import javax.inject.Singleton;

import dagger.Component;


/**
 * Created by ogre on 2015-10-04
 */

@Component(modules = {MyAppDaggerModule.class,
        AppInfoDaggerModule.class,
        SessionDaggerModule.class,
        UnitDaggerModule.class,
        NetworkInfoProviderDaggerModule.class,
        ExchangeDaggerModule.class,
        LoginModule.class
})
@Singleton
public interface MyAppDaggerComponent {
    void inject(App app);

    void inject(ActMain act);

    void inject(ActRegister act);

    void inject(ActSelectLogin act);

    void inject(ActLogin act);

    void inject(ActScreenName act);

    void inject(ActLoginFacebook act);

    void inject(ActLoginGoogle act);
}
