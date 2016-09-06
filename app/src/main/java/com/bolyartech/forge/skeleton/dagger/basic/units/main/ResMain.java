package com.bolyartech.forge.skeleton.dagger.basic.units.main;

import com.bolyartech.forge.android.app_unit.MorcActivityInterface;
import com.bolyartech.forge.android.app_unit.MultiOperationResidentComponent;


/**
 * Created by ogre on 2015-10-05
 */
public interface ResMain extends MultiOperationResidentComponent<ResMain.Operation>,
        RiMain {


    enum Operation {
        AUTO_REGISTERING,
        LOGIN
    }


    enum LoginError {
        INVALID_LOGIN,
        FAILED,
        UPGRADE_NEEDED
    }


    enum AutoregisteringError {
        FAILED,
        UPGRADE_NEEDED
    }
}
