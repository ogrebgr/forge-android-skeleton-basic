package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.rc_task.simple.SimpleAbstractRcTask;
import com.bolyartech.forge.base.rc_task.simple.SimpleRcTaskResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;

import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.inject.Inject;


public class LogoutTaskImpl extends SimpleAbstractRcTask<Void> implements LogoutTask {
    private final ForgeExchangeHelper forgeExchangeHelper;
    private final SessionForgeExchangeExecutor sessionExecutor;
    private final Session session;

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());


    @Inject
    public LogoutTaskImpl(ForgeExchangeHelper forgeExchangeHelper, SessionForgeExchangeExecutor sessionExecutor, Session session) {
        super(TASK_ID);
        this.forgeExchangeHelper = forgeExchangeHelper;
        this.sessionExecutor = sessionExecutor;
        this.session = session;
    }


    @Override
    public void execute() {
        ForgePostHttpExchangeBuilder b = forgeExchangeHelper.createForgePostHttpExchangeBuilder("logout");
        try {
            sessionExecutor.execute(b.build());
            session.logout();
            setTaskResult(new SimpleRcTaskResult<>(null));
        } catch (IOException | ResultProducer.ResultProducerException e) {
            logger.warn("Error during logout {}", e.getMessage());
        }
    }
}
