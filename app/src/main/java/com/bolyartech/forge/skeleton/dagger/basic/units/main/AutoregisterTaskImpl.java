package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.rc_task.AbstractRcTask;
import com.bolyartech.forge.base.rc_task.RcTaskResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.inject.Inject;


public class AutoregisterTaskImpl extends AbstractRcTask<RcTaskResult<Void, Integer>> implements AutoregisterTask {
    private final AppConfiguration mAppConfiguration;
    private final ForgeExchangeHelper forgeExchangeHelper;
    private final SessionForgeExchangeExecutor sessionExecutor;
    private final Session session;
    private final CurrentUserHolder currentUserHolder;


    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile HttpExchange<ForgeExchangeResult> httpExchange;


    @Inject
    public AutoregisterTaskImpl(AppConfiguration mAppConfiguration,
                                ForgeExchangeHelper forgeExchangeHelper,
                                SessionForgeExchangeExecutor sessionExecutor,
                                Session session,
                                CurrentUserHolder currentUserHolder) {

        super(TASK_ID);
        this.mAppConfiguration = mAppConfiguration;
        this.forgeExchangeHelper = forgeExchangeHelper;
        this.sessionExecutor = sessionExecutor;
        this.session = session;
        this.currentUserHolder = currentUserHolder;
    }


    @Override
    public void execute() {
        ForgePostHttpExchangeBuilder b = forgeExchangeHelper.createForgePostHttpExchangeBuilder("autoregister");
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", mAppConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");

        httpExchange = b.build();

        try {
            ForgeExchangeResult rez = sessionExecutor.execute(httpExchange);
            if (rez.getCode() == BasicResponseCodes.OK) {
                try {
                    JSONObject jobj = new JSONObject(rez.getPayload());

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        int sessionTtl = jobj.getInt("session_ttl");
                        session.startSession(sessionTtl);

                        currentUserHolder.setCurrentUser(
                                new CurrentUser(sessionInfo.getLong("user_id"),
                                        sessionInfo.optString("screen_name", null)));

                        LoginPrefs lp = mAppConfiguration.getLoginPrefs();

                        lp.setUsername(jobj.getString("username"));
                        lp.setPassword(jobj.getString("password"));
                        lp.setManualRegistration(false);
                        lp.save();

                        setTaskResult(RcTaskResult.createSuccessResult(null));
                    } else {
                        logger.error("Missing session info");
                        setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                    }
                } catch (JSONException e) {
                    logger.warn("Register auto exchange failed because cannot parse JSON");
                    setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                }
            } else {
                setTaskResult(RcTaskResult.createErrorResult(rez.getCode()));
            }
        } catch (IOException | ResultProducer.ResultProducerException e) {
            logger.error("Error executing autoregister: {}", e.getMessage());
            setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
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
