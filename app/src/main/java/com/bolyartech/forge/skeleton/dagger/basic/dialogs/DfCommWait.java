package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.bolyartech.forge.skeleton.dagger.basic.R;

import org.slf4j.LoggerFactory;


public class DfCommWait extends DialogFragment {
    public static final String DIALOG_TAG = "Df_CommWait";
    private final org.slf4j.Logger mLogger = LoggerFactory.getLogger(this.getClass());
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
            mListener.onCommWaitDialogCancelled();
        }
    }


    public interface Listener {
        void onCommWaitDialogCancelled();
    }
}
