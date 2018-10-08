package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.rc_task.failing.FailingRcTaskResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.misc.SessionFailingTask;

import java.io.IOException;

import javax.inject.Inject;


public class ScreenNameTaskImpl extends SessionFailingTask<Integer> implements ScreenNameTask {
    private final CurrentUserHolder currentUserHolder;

    private String screenName;

    private volatile HttpExchange<ForgeExchangeResult> httpExchange;


    @Inject
    public ScreenNameTaskImpl(ForgeExchangeHelper forgeExchangeHelper,
                              SessionForgeExchangeExecutor sessionExecutor,
                              CurrentUserHolder currentUserHolder) {

        super(TASK_ID, forgeExchangeHelper, sessionExecutor);
        this.currentUserHolder = currentUserHolder;
    }


    @Override
    public void init(String screenName) {
        this.screenName = screenName;
    }


    @Override
    public void execute() {
        ForgePostHttpExchangeBuilder b = getForgeExchangeHelper().createForgePostHttpExchangeBuilder("screen_name");
        b.addPostParameter("screen_name", screenName);
        try {
            httpExchange = b.build();
            ForgeExchangeResult rez = getSessionExecutor().execute(httpExchange);
            if (rez.getCode() == BasicResponseCodes.OK) {
                CurrentUser user = currentUserHolder.getCurrentUser();
                currentUserHolder.setCurrentUser(new CurrentUser(user.getId(), screenName));
                setTaskResult(new FailingRcTaskResult<>(true, null));
            } else {
                setTaskResult(new FailingRcTaskResult<>(false, rez.getCode()));
            }
        } catch (IOException | ResultProducer.ResultProducerException e) {
            setTaskResult(new FailingRcTaskResult<>(false, BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
        }
    }


    @Override
    public void cancel() {
        super.cancel();
        if (httpExchange != null) {
            httpExchange.cancel();
        }
    }
}
