package com.bolyartech.forge.skeleton.dagger.basic.misc;

import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.rc_task.AbstractRcTask;
import com.bolyartech.forge.base.rc_task.RcTaskResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;


abstract public class SessionTask<SUCCESS_VALUE, ERROR_VALUE> extends AbstractRcTask<RcTaskResult<SUCCESS_VALUE, ERROR_VALUE>> {
    private final ForgeExchangeHelper forgeExchangeHelper;
    private final SessionForgeExchangeExecutor sessionExecutor;


    protected SessionTask(int id, ForgeExchangeHelper forgeExchangeHelper, SessionForgeExchangeExecutor sessionExecutor) {
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
