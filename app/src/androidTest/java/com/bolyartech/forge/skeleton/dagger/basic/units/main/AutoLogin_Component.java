package com.bolyartech.forge.skeleton.dagger.basic.units.main;


import com.bolyartech.forge.skeleton.dagger.basic.dagger.AppInfoDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.NetworkInfoProviderDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.SessionDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitDaggerModule;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {MyAppDaggerModule.class,
        AppInfoDaggerModule.class,
        SessionDaggerModule.class,
        UnitDaggerModule.class,
        NetworkInfoProviderDaggerModule.class,
        AutoLogin_ExchangeDaggerModule.class,
})
@Singleton
public interface AutoLogin_Component extends MyAppDaggerComponent {

}
