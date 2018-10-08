package com.bolyartech.forge.skeleton.dagger.basic.units.register;

import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.misc.StringUtils;
import com.bolyartech.forge.base.rc_task.failing.FailingRcTaskResult;
import com.bolyartech.forge.base.session.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.misc.SessionFailingTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.inject.Inject;


public class RegistrationTaskImpl extends SessionFailingTask<Integer> implements RegistrationTask {
    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AppConfiguration appConfiguration;
    private final Session session;
    private final CurrentUserHolder currentUserHolder;


    private String username;
    private String password;
    private String screenName;

    private volatile HttpExchange<ForgeExchangeResult> httpExchange;


    @Inject
    public RegistrationTaskImpl(ForgeExchangeHelper forgeExchangeHelper,
                                SessionForgeExchangeExecutor sessionExecutor,
                                AppConfiguration appConfiguration,
                                Session session,
                                CurrentUserHolder currentUserHolder) {

        super(TASK_ID, forgeExchangeHelper, sessionExecutor);
        this.appConfiguration = appConfiguration;
        this.session = session;
        this.currentUserHolder = currentUserHolder;
    }


    @Override
    public void init(String username, String password, String screenName) {
        this.username = username;
        this.password = password;
        this.screenName = screenName;
    }


    @Override
    public void execute() {
        if (StringUtils.isEmpty(appConfiguration.getLoginPrefs().getUsername())) {
            normalRegistration(username, password, screenName);
        } else {
            if (!appConfiguration.getLoginPrefs().isManualRegistration()) {
                postAutoRegistration(username, password, screenName);
            } else {
                throw new IllegalStateException("Task should not be executed in this case");
            }
        }
    }


    @Override
    public void cancel() {
        super.cancel();
        if (httpExchange != null) {
            httpExchange.cancel();
        }
    }


    private void postAutoRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = getForgeExchangeHelper().createForgePostHttpExchangeBuilder("register_postauto");

        LoginPrefs lp = appConfiguration.getLoginPrefs();
        b.addPostParameter("username", lp.getUsername());
        b.addPostParameter("password", lp.getPassword());
        b.addPostParameter("new_username", username);
        b.addPostParameter("new_password", password);
        b.addPostParameter("screen_name", screenName);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", appConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");
        b.addPostParameter("do_login", "1");

        httpExchange = b.build();
        try {
            ForgeExchangeResult rez = getSessionExecutor().execute(httpExchange);

            if (handleRegistrationCommon1(rez)) {
                CurrentUser old = currentUserHolder.getCurrentUser();
                if (currentUserHolder.getCurrentUser().hasScreenName()) {
                    currentUserHolder.setCurrentUser(new CurrentUser(old.getId(),
                            currentUserHolder.getCurrentUser().getScreenName()));
                } else {
                    currentUserHolder.setCurrentUser(new CurrentUser(old.getId(), screenName));
                }

                handleRegistrationCommon2();
            }
        } catch (IOException | ResultProducer.ResultProducerException e) {
            logger.warn("Register exchange failed");
            setTaskResult(new FailingRcTaskResult<>(false, BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
        }
    }


    private void normalRegistration(String username, String password, String screenName) {
        ForgePostHttpExchangeBuilder b = getForgeExchangeHelper().createForgePostHttpExchangeBuilder("register");

        b.addPostParameter("username", username);
        b.addPostParameter("password", password);
        b.addPostParameter("screen_name", screenName);
        b.addPostParameter("app_type", "1");
        b.addPostParameter("app_version", appConfiguration.getAppVersion());
        b.addPostParameter("session_info", "1");
        b.addPostParameter("do_login", "1");

        httpExchange = b.build();
        try {
            ForgeExchangeResult rez = getSessionExecutor().execute(httpExchange);
            if (handleRegistrationCommon1(rez)) {
                try {
                    JSONObject jobj = new JSONObject(rez.getPayload());
                    int sessionTtl = jobj.getInt("session_ttl");

                    JSONObject sessionInfo = jobj.optJSONObject("session_info");
                    if (sessionInfo != null) {
                        session.startSession(sessionTtl);

                        currentUserHolder.setCurrentUser(new CurrentUser(sessionInfo.getLong("user_id"),
                                sessionInfo.optString("screen_name", null)));


                        handleRegistrationCommon2();
                    } else {
                        setTaskResult(new FailingRcTaskResult<>(false, BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                    }
                } catch (JSONException e) {
                    logger.warn("Register exchange failed because cannot parse JSON");
                    setTaskResult(new FailingRcTaskResult<>(false, BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
                }
            }
        } catch (IOException | ResultProducer.ResultProducerException e) {
            logger.warn("Register exchange failed");
            setTaskResult(new FailingRcTaskResult<>(false, BasicResponseCodes.Errors.UNSPECIFIED_ERROR));
        }
    }


    private boolean handleRegistrationCommon1(ForgeExchangeResult result) {
        int code = result.getCode();

        if (code != BasicResponseCodes.OK) {
            logger.warn("Register exchange failed with code {}", code);
            setTaskResult(new FailingRcTaskResult<>(false, code));

            return false;
        }

        return true;
    }


    private void handleRegistrationCommon2() {
        LoginPrefs lp = appConfiguration.getLoginPrefs();
        lp.setUsername(username);
        lp.setPassword(password);
        lp.setManualRegistration(true);
        lp.save();

        logger.debug("App register OK");
        setTaskResult(new FailingRcTaskResult<>(true, null));
    }
}
