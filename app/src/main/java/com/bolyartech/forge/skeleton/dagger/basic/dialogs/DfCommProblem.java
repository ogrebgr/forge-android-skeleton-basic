package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.bolyartech.forge.skeleton.dagger.basic.R;


public class DfCommProblem extends DialogFragment {
    public static final String DIALOG_TAG = "Df_CannotSendData";


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
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setIcon(R.drawable.dlg_icon_failure);
        b.setTitle(R.string.dlg__comm_problem_title);
        b.setMessage(R.string.dlg__comm_problem_msg);
        b.setCancelable(false);
        b.setNeutralButton(R.string.global_btn_close, null);
        return b.create();
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            mListener.onCommProblemClosed();
        }
    }


    public interface Listener {
        void onCommProblemClosed();
    }
}
