package com.bolyartech.forge.skeleton.dagger.basic.units.login;

import android.support.annotation.NonNull;

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
import com.bolyartech.forge.skeleton.dagger.basic.app.AuthenticationResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;
import com.bolyartech.scram_sasl.client.ScramClientFunctionality;
import com.bolyartech.scram_sasl.common.ScramException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.inject.Inject;


public class LoginTaskImpl extends AbstractRcTask<RcTaskResult<Void, Integer>> implements LoginTask {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ForgeExchangeHelper forgeExchangeHelper;
    private final ScramClientFunctionality scramClientFunctionality;
    private final SessionForgeExchangeExecutor sessionExecutor;

    private final Session session;
    private final CurrentUserHolder currentUserHolder;
    private final AppConfiguration appConfiguration;


    private String username;
    private String password;
    private boolean autologin;


    @Inject
    public LoginTaskImpl(ForgeExchangeHelper forgeExchangeHelper,
                         ScramClientFunctionality scramClientFunctionality,
                         SessionForgeExchangeExecutor sessionExecutor,
                         Session session,
                         CurrentUserHolder currentUserHolder,
                         AppConfiguration appConfiguration) {

        super(TASK_ID);
        this.forgeExchangeHelper = forgeExchangeHelper;
        this.scramClientFunctionality = scramClientFunctionality;
        this.sessionExecutor = sessionExecutor;
        this.session = session;
        this.currentUserHolder = currentUserHolder;
        this.appConfiguration = appConfiguration;
    }


    @Override
    public void execute() {
        try {
            if (this.username == null) {
                throw new IllegalStateException("username is null. Did you forgot to call init() " +
                        "before execute()?");
            }

            HttpExchange<ForgeExchangeResult> login1exchange = createLoginStep1Exchange(username);
            ForgeExchangeResult rez1 = sessionExecutor.execute(login1exchange);

            if (rez1.getCode() != BasicResponseCodes.OK) {
                logger.warn("login1exchange not successful");
                setTaskResult(RcTaskResult.createErrorResult(rez1.getCode()));
                return;
            }

            if (isCancelled()) {
                return;
            }

            String serverFirst = rez1.getPayload();
            String clientFinal = scramClientFunctionality.prepareFinalMessage(password, serverFirst);
            if (clientFinal == null) {
                logger.warn("Invalid login 1");
                setTaskResult(RcTaskResult.createErrorResult(AuthenticationResponseCodes.Errors.INVALID_LOGIN));
                return;
            }


            HttpExchange<ForgeExchangeResult> login2exchange = createLoginStep2Exchange(clientFinal);

            ForgeExchangeResult rez2 = sessionExecutor.execute(login2exchange);
            if (rez2.getCode() != BasicResponseCodes.OK) {
                logger.warn("login1exchange not successful");
                setTaskResult(RcTaskResult.createErrorResult(rez1.getCode()));
                return;
            }

            if (isCancelled()) {
                return;
            }


            JSONObject jsonObj = new JSONObject(rez2.getPayload());
            int sessionTtl = jsonObj.getInt("session_ttl");
            JSONObject sessionInfo = jsonObj.optJSONObject("session_info");
            String serverFinal = jsonObj.getString("final_message");

            if (!scramClientFunctionality.checkServerFinalMessage(serverFinal)) {
                logger.warn("Invalid login 2");
                setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                return;
            }

            if (sessionInfo == null) {
                logger.error("sessionInfo is empty.");
                setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                return;
            }

            session.startSession(sessionTtl);

            currentUserHolder.setCurrentUser(
                    new CurrentUser(sessionInfo.getLong("user_id"),
                            sessionInfo.optString("screen_name", null)));

            logger.debug("App login OK");

//            appConfiguration.getAppPrefs().save();

            LoginPrefs lp = appConfiguration.getLoginPrefs();
            lp.setUsername(username);
            lp.setPassword(password);

            if (!autologin) {
                lp.setManualRegistration(true);
            }
            lp.save();

            setTaskResult(RcTaskResult.createSuccessResult(null));
        } catch (ScramException | ResultProducer.ResultProducerException | IOException |
                JSONException e) {
            logger.error(e.getMessage());
            setTaskResult(RcTaskResult.createErrorResult(BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
        }
    }


    public void init(@NonNull String username, @NonNull String password, boolean autologin) {
        this.username = username;
        this.password = password;
        this.autologin = autologin;
    }


    private HttpExchange<ForgeExchangeResult> createLoginStep1Exchange(String username) throws ScramException {
        ForgePostHttpExchangeBuilder step1builder = forgeExchangeHelper.
                createForgePostHttpExchangeBuilder("login");

        String clientFirst = scramClientFunctionality.prepareFirstMessage(username);
        step1builder.addPostParameter("app_type", "");
        step1builder.addPostParameter("app_version", "1");
        step1builder.addPostParameter("step", "1");
        step1builder.addPostParameter("data", clientFirst);
        return step1builder.build();
    }


    private HttpExchange<ForgeExchangeResult> createLoginStep2Exchange(String clientFinal) {
        ForgePostHttpExchangeBuilder step2builder = forgeExchangeHelper.
                createForgePostHttpExchangeBuilder("login");

        step2builder.addPostParameter("step", "2");
        step2builder.addPostParameter("data", clientFinal);

        return step2builder.build();
    }
}