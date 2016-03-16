package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import android.content.Context;

import com.bolyartech.forge.android.app_unit.StateManager;
import com.bolyartech.forge.android.app_unit.StateManagerImpl;
import com.bolyartech.forge.android.misc.AndroidEventPoster;
import com.bolyartech.forge.android.misc.NetworkInfoProvider;
import com.bolyartech.forge.android.mvp.Model;
import com.bolyartech.forge.base.exchange.ForgeExchangeResult;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.task.ForgeExchangeManager;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.AppPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.LoginPrefs;
import com.bolyartech.forge.skeleton.dagger.basic.app.ResponseCodes;
import com.bolyartech.forge.skeleton.dagger.basic.app.Session;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionResidentComponent;
import com.bolyartech.forge.skeleton.dagger.basic.misc.ForApplication;
import com.bolyartech.forge.skeleton.dagger.basic.misc.LoginMethod;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;


/**
 * Created by ogre on 2015-11-17 17:29
 */
public class Res_MainImpl extends SessionResidentComponent {


    @Override
    public void onSessionExchangeOutcome(long exchangeId, boolean isSuccess, ForgeExchangeResult result) {
    }


    @Override
    public Model getModel() {
        return null;
    }
}
