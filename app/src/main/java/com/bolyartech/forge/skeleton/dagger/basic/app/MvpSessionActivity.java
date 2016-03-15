package com.bolyartech.forge.skeleton.dagger.basic.app;


import android.content.Intent;


abstract public class MvpSessionActivity extends SessionActivity {
    @Override
    public void onResume() {
        super.onResume();

        if (!isFinishing()) {

        }
    }


    @Override
    public void onPause() {
        super.onPause();

        if (!isFinishing()) {

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!isFinishing()) {

        }
    }
}
