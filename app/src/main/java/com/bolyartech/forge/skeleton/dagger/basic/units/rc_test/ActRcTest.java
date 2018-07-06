package com.bolyartech.forge.skeleton.dagger.basic.units.rc_test;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.bolyartech.forge.android.app_unit.rc_task.task.RcTaskResult;
import com.bolyartech.forge.android.misc.ViewUtils;
import com.bolyartech.forge.skeleton.dagger.basic.R;
import com.bolyartech.forge.skeleton.dagger.basic.app.RctUnitActivity;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.DfGenericWait;
import com.bolyartech.forge.skeleton.dagger.basic.dialogs.MyAppDialogs;

import javax.inject.Inject;

import dagger.Lazy;


public class ActRcTest extends RctUnitActivity<ResRcTest> implements DfGenericWait.Listener {

    @Inject
    Lazy<ResRcTest> ResRcTestLazy;
    private TextView tvResult;


    @Override
    public void handleResidentIdleState() {
        MyAppDialogs.hideGenericWaitDialog(getFragmentManager());
    }


    @Override
    public void handleResidentBusyState() {
        MyAppDialogs.showGenericWaitDialog(getFragmentManager());
        if (getRes().getCurrentTask().getId() == 1) {

        }
    }


    @Override
    public void handleResidentEndedState() {
        MyAppDialogs.hideGenericWaitDialog(getFragmentManager());

        switch (getRes().getCurrentTask().getId()) {
            case 1:
                handleTest1result();
                break;
            case 2:
                handleTest2result();
                break;
            default:
                throw new AssertionError("Unexpected task ID");
        }
    }


    @NonNull
    @Override
    public ResRcTest createResidentComponent() {
        return ResRcTestLazy.get();
    }


    @Override
    public void onGenericDialogCancelled() {
        getResident().abort();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDependencyInjector().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act__rc_test);

        ViewUtils.initButton(getWindow().getDecorView(), R.id.btn_task1, v -> getRes().test1());


        ViewUtils.initButton(getWindow().getDecorView(), R.id.btn_task2, v -> getRes().test2());


        tvResult = ViewUtils.findTextViewX(getWindow().getDecorView(), R.id.tv_result);
    }


    private void handleTest2result() {
        RcTaskResult<String, Void> res = getRes().getTask2Result();

        if (res != null) {
            if (res.isSuccess()) {
                tvResult.setText(res.getSuccessValue());
            } else {
                tvResult.setText("Patka!!!");
            }

        }
    }


    private void handleTest1result() {
        RcTaskResult<String, Integer> res = getRes().getTask1Result();

        if (res != null) {
            if (res.isSuccess()) {
                tvResult.setText(res.getSuccessValue());
            } else {
                tvResult.setText("Patka: " + res.getErrorValue());
            }
        }
    }
}
