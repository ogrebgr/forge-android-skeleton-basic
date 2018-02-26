/*
 * Copyright (C) 2015-2016 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.android.app_unit.UnitManagerImpl;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeManagerListener;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.session.Session;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * @deprecated
 */
@Singleton
public class AppUnitManager extends UnitManagerImpl implements ForgeExchangeManagerListener {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());


    private final Session mSession;


    @Inject
    public AppUnitManager(Session session) {
        mSession = session;
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult forgeExchangeResult) {
        if (forgeExchangeResult != null) {
            mLogger.debug("Forge exchange returned with code {}", forgeExchangeResult.getCode());
        }
        if (isSuccess) {
            mSession.prolong();
        }

        if (getActiveResidentComponent() instanceof ForgeExchangeManagerListener) {
            ForgeExchangeManagerListener l = (ForgeExchangeManagerListener) getActiveResidentComponent();
            l.onExchangeOutcome(exchangeId,isSuccess, forgeExchangeResult);
        }
    }
}
