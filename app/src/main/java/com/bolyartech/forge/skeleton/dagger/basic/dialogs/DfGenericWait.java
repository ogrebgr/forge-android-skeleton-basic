package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.bolyartech.forge.skeleton.dagger.basic.R;


/**
 * Created by ogre on 2015-08-12
 */
public class DfGenericWait extends DialogFragment {
    public static final String DIALOG_TAG = "Df_GenericWait";
    private Listener mListener;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog d = new ProgressDialog(getActivity());
        d.setIndeterminate(true);
        d.setCancelable(true);
        d.setMessage(getString(R.string.dlg__generic_wait));
        d.setCanceledOnTouchOutside(false);
        return d;
    }


    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        if (mListener != null) {
            mListener.onGenericDialogCancelled();
        }
    }


    public interface Listener {
        void onGenericDialogCancelled();
    }
}
