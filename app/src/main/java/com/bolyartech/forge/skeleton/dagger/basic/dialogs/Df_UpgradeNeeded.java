package com.bolyartech.forge.skeleton.dagger.basic.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

import com.bolyartech.forge.skeleton.dagger.basic.R;


public class Df_UpgradeNeeded extends DialogFragment {
    public static final String DIALOG_TAG = "upgrade needed";


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());
        b.setIcon(R.drawable.dlg_icon_failure);
        b.setTitle(R.string.dlg__upgrade_needed__title);
        b.setMessage(R.string.dlg__upgrade_needed__msg);
        b.setCancelable(false);
        b.setNeutralButton(R.string.global_btn_close, null);
        Dialog ret = b.create();
        ret.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                getActivity().finish();
            }
        });

        return ret;
    }
}
