package com.bolyartech.forge.skeleton.dagger.basic.app;

import com.bolyartech.forge.app_unit.UnitManager;
import com.bolyartech.forge.exchange.ExchangeOutcome;
import com.bolyartech.forge.exchange.ForgeExchangeFunctionality;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.misc.ForgeExchangeManagerImpl;

import javax.inject.Inject;


/**
 * Created by ogre on 2015-11-15 15:08
 */
public class MyAppExchangeManager extends ForgeExchangeManagerImpl {

    private Session mSession;

    private UnitManager mUnitManager;


    @Inject
    public MyAppExchangeManager(UnitManager unitManager,
                                ForgeExchangeFunctionality forgeExchangeFunctionality,
                                Session session) {
        super(unitManager, forgeExchangeFunctionality);
        mSession = session;
    }


    @Override
    public void onExchangeCompleted(ExchangeOutcome<ForgeExchangeResult> outcome, long exchangeId) {
        if (SessionImpl.isNeedLogin(outcome)) {
            mSession.setIsLoggedIn(false);
        } else {
            mSession.prolong();
        }

        super.onExchangeCompleted(outcome, exchangeId);
    }
}
