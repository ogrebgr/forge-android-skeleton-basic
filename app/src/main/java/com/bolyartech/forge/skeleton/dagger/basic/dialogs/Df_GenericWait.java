package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.bolyartech.forge.skeleton.dagger.basic.R;


/**
 * Created by ogre on 2015-08-12
 */
public class Df_GenericWait extends DialogFragment {
    public static final String DIALOG_TAG = "Df_GenericWait";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog d = new ProgressDialog(getActivity());
        d.setIndeterminate(true);
        d.setCancelable(true);
        d.setMessage(getString(R.string.dlg__generic_wait));
        d.setCanceledOnTouchOutside(false);
        return d;
    }
}
