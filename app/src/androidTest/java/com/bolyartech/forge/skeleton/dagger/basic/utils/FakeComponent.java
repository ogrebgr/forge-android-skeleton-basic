package com.bolyartech.forge.skeleton.dagger.basic.utils;


import com.bolyartech.forge.skeleton.dagger.basic.dagger.AppInfoDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerComponent;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.MyAppDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.NetworkInfoProviderDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.SessionDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.dagger.UnitDaggerModule;
import com.bolyartech.forge.skeleton.dagger.basic.units.main.AutoLogin_ExchangeDaggerModule;

import javax.inject.Singleton;

import dagger.Component;


@Component(modules = {FakeMyAppDaggerModule.class,
        AppInfoDaggerModule.class,
        FakeSessionDaggerModule.class,
        FakeUnitDaggerModule.class,
        NetworkInfoProviderDaggerModule.class,
        FakeExchangeDaggerModule.class,
})
@Singleton
public interface FakeComponent extends MyAppDaggerComponent {

}
