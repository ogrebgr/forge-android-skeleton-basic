package com.bolyartech.forge.skeleton.dagger.basic.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.bolyartech.forge.skeleton.dagger.basic.R;


public class DfNoInet extends DialogFragment {
    public static final String DIALOG_TAG = "Df_NoInet";

    private Listener mListener;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() instanceof Listener) {
            mListener = (Listener) getActivity();
        }
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onNoInetDialogClosed();
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setIcon(R.drawable.dlg_icon_failure);
        b.setTitle(R.string.dlg__no_inet__title);
        b.setMessage(R.string.dlg__no_inet__msg);
        b.setCancelable(false);
        b.setNeutralButton(R.string.global_btn_close, null);
        return b.create();
    }


    public interface Listener {
        void onNoInetDialogClosed();
    }
}
