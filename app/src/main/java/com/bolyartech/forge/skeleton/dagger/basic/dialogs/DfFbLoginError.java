package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.bolyartech.forge.skeleton.dagger.basic.R;


/**
 * Created by ogre on 2015-09-26
 */
public class DfFbLoginError extends DialogFragment {
    public static final String DIALOG_TAG = "Df_FbLoginError";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setIcon(R.drawable.dlg_icon_failure);
        b.setTitle(R.string.dlg__fb_login_error__title);
        b.setMessage(R.string.dlg__fb_login_error__msg);
        b.setCancelable(false);
        b.setNeutralButton(R.string.global_btn_close, null);
        return b.create();
    }
}