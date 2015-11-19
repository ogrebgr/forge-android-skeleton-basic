package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

/**
 * Created by ogre on 2015-09-28
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.bolyartech.forge.skeleton.dagger.basic.R;


public class Df_GoogleLoginError extends DialogFragment {
    public static final String DIALOG_TAG = "Df_GoogleLoginError";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setIcon(R.drawable.dlg_icon_failure);
        b.setTitle(R.string.dlg__google_login_error__title);
        b.setMessage(R.string.dlg__google_login_error__msg);
        b.setCancelable(false);
        b.setNeutralButton(R.string.global_btn_close, null);
        return b.create();
    }
}