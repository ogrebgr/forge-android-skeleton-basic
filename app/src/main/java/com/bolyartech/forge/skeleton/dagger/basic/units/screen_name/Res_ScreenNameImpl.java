/*
 * Copyright (C) 2012-2016 Ognyan Bankov
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

import com.bolyartech.forge.exchange.ForgeExchangeBuilder;
import com.bolyartech.forge.exchange.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.Ev_StateChanged;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.task.ForgeExchangeManager;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class Res_ScreenNameImpl extends SessionResidentComponent implements Res_ScreenName  {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private final StateManager mStateManager = new StateManager();

    private long mExchangeId;

    private ResponseCodes.Errors mLastError;


    @Inject
    public Res_ScreenNameImpl() {
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mExchangeId == exchangeId) {
            mLastError = null;
            if (isSuccess) {
                int code = result.getCode();

                if (code == ResponseCodes.Oks.SCREEN_NAME_OK.getCode()) {
                    mStateManager.switchToState(State.SCREEN_NAME_OK);
                } else {
                    mLogger.warn("Screen name exchange failed with code {}", code);
                    mStateManager.switchToState(State.SCREEN_NAME_FAIL);
                }
            } else {
                mLogger.warn("Screen name exchange failed");
                mStateManager.switchToState(State.SCREEN_NAME_FAIL);
            }
        }
    }


    @Override
    public State getState() {
        return mStateManager.getState();
    }


    @Override
    public void screenName(String screenName) {
        if (mStateManager.getState() == State.IDLE) {
            mStateManager.switchToState(State.PROCESSING);

            ForgeExchangeBuilder b = createForgeExchangeBuilder("screen_name.php");

            b.addPostParameter("screen_name", screenName);

            ForgeExchangeManager em = getForgeExchangeManager();
            mExchangeId = em.generateTaskId();
            em.executeExchange(b.build(), mExchangeId);
        } else {
            mLogger.error("screenName() called not in IDLE state. Ignoring.");
        }
    }


    @Override
    public void resetState() {
        mStateManager.switchToState(State.IDLE);
    }


    @Override
    public ResponseCodes.Errors getLastError() {
        return mLastError;
    }


    private class StateManager {
        private State mState = State.IDLE;


        public State getState() {
            return mState;
        }


        public void switchToState(State state) {
            mState = state;
            postEvent(new Ev_StateChanged());
        }
    }
}
