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

import com.bolyartech.forge.android.app_unit.AbstractSideEffectOperationResidentComponent;
import com.bolyartech.forge.android.app_unit.OperationResidentComponent;
import com.bolyartech.forge.base.exchange.ForgeExchangeManager;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.BasicResponseCodes;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUser;
import com.bolyartech.forge.skeleton.dagger.basic.app.CurrentUserHolder;

import org.slf4j.LoggerFactory;

import javax.inject.Inject;


public class ResScreenNameImpl extends AbstractSideEffectOperationResidentComponent<Void, Integer> implements ResScreenName {
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private volatile long mExchangeId;

    private String mScreenName;

    private final ForgeExchangeHelper mForgeExchangeHelper;

    @Inject
    CurrentUserHolder mCurrentUserHolder;


    @Inject
    public ResScreenNameImpl(ForgeExchangeHelper forgeExchangeHelper) {

        mForgeExchangeHelper = forgeExchangeHelper;
    }


    @Override
    public void onExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
        if (mExchangeId == exchangeId) {
            if (isSuccess) {
                int code = result.getCode();

                if (code == BasicResponseCodes.OK) {
                    CurrentUser user = mCurrentUserHolder.getCurrentUser();
                    mCurrentUserHolder.setCurrentUser(new CurrentUser(user.getId(), mScreenName));

                    switchToEndedStateSuccess(null);
                } else {
                    mLogger.warn("Screen name exchange failed with code {}", code);
                    switchToEndedStateFail(code);
                }
            } else {
                mLogger.warn("Screen name exchange failed");
                switchToEndedStateFail(null);
            }
        }
    }


    @Override
    public void screenName(String screenName) {
        if (getOpState() == OperationResidentComponent.OpState.IDLE) {
            switchToBusyState();

            ForgePostHttpExchangeBuilder b = mForgeExchangeHelper.createForgePostHttpExchangeBuilder("screen_name");
            mScreenName = screenName;
            b.addPostParameter("screen_name", screenName);

            ForgeExchangeManager em = mForgeExchangeHelper.getExchangeManager();
            mExchangeId = em.generateTaskId();
            em.executeExchange(b.build(), mExchangeId);
        } else {
            mLogger.error("screenName() called not in IDLE state. Ignoring.");
        }
    }
}
