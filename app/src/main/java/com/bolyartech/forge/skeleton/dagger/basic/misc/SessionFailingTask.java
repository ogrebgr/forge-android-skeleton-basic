package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.rc_task.failing.FailingAbstractRcTask;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;


abstract public class SessionFailingTask<ERROR_VALUE> extends FailingAbstractRcTask<ERROR_VALUE> {
    private final ForgeExchangeHelper forgeExchangeHelper;
    private final SessionForgeExchangeExecutor sessionExecutor;


    public SessionFailingTask(int id, ForgeExchangeHelper forgeExchangeHelper, SessionForgeExchangeExecutor sessionExecutor) {
        super(id);
        this.forgeExchangeHelper = forgeExchangeHelper;
        this.sessionExecutor = sessionExecutor;
    }


    protected ForgeExchangeHelper getForgeExchangeHelper() {
        return forgeExchangeHelper;
    }


    protected SessionForgeExchangeExecutor getSessionExecutor() {
        return sessionExecutor;
    }
}
