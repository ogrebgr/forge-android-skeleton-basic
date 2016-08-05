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
package com.bolyartech.forge.skeleton.dagger.basic.units.screen_name;

import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.base.exchange.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppConfiguration;
import com.bolyartech.forge.skeleton.dagger.basic.app.BasicResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Res_ScreenNameImpl extends SessionResidentComponent<Res_ScreenName.State> implements Res_ScreenName {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private volatile long mExchangeId;

    private BasicResponseCodes.Errors mLastError;

    private String mScreenName;


    @Inject
    public Res_ScreenNameImpl(AppConfiguration appConfiguration,
                              ForgeExchangeHelper forgeExchangeHelper,
                              Session session,
                              NetworkInfoProvider networkInfoProvider) {

        super(State.IDLE, forgeExchangeHelper, session, networkInfoProvider);
    }


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mExchangeId == exchangeId) {
            mLastError = null;
            if (isSuccess) {
                int code = result.getCode();

                if (code == BasicResponseCodes.Oks.OK.getCode()) {
                    getSession().getInfo().setScreenName(mScreenName);
                    switchToState(State.SCREEN_NAME_OK);
                } else {
                    mLastError = BasicResponseCodes.Errors.fromInt(code);
                    mLogger.warn("Screen name exchange failed with code {}", code);
                    switchToState(State.SCREEN_NAME_FAIL);
                }
            } else {
                mLogger.warn("Screen name exchange failed");
                switchToState(State.SCREEN_NAME_FAIL);
            }
        }
    }


    @Override
    public void screenName(String screenName) {
        if (getState() == State.IDLE) {
            switchToState(State.PROCESSING);

            ForgePostHttpExchangeBuilder b = createForgePostHttpExchangeBuilder("screen_name");
            mScreenName = screenName;
            b.addPostParameter("screen_name", screenName);

            ForgeExchangeManager em = getForgeExchangeManager();
            mExchangeId = em.generateTaskId();
            em.executeExchange(b.build(), mExchangeId);
        } else {
            mLogger.error("screenName() called not in IDLE state. Ignoring.");
        }
    }


    @Override
    public BasicResponseCodes.Errors getLastError() {
        return mLastError;
    }


    @Override
    public void stateHandled() {
        if (isInOneOfStates(State.SCREEN_NAME_OK, State.SCREEN_NAME_FAIL)) {
            resetState();
        }
    }
}
