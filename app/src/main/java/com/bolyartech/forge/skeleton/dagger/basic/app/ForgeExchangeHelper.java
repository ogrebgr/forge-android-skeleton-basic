package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.base.exchange.builders.ForgeGetHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;


public interface ForgeExchangeHelper {
    ForgePostHttpExchangeBuilder createForgePostHttpExchangeBuilder(String endpoint);
    ForgeGetHttpExchangeBuilder createForgeGetHttpExchangeBuilder(String endpoint);
    ForgeExchangeManager getExchangeManager();
}
