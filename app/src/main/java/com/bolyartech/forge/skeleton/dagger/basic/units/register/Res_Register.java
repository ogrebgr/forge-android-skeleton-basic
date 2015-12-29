package com.bolyartech.forge.skeleton.dagger.basic.units.register;

/**
 * Created by ogre on 2015-12-11 21:44
 */
public interface Res_Register {
    enum State {
        IDLE,
        REGISTERING,
        REGISTER_OK,
        REGISTER_FAIL
    }


    void register();

}
