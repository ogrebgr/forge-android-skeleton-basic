package com.bolyartech.forge.skeleton.dagger.basic.units.rc_test;

import android.support.annotation.NonNull;

import com.bolyartech.forge.android.app_unit.rc_task.AbstractRctResidentComponent;
import com.bolyartech.forge.android.app_unit.rc_task.executor.RcTaskExecutor;
import com.bolyartech.forge.base.exchange.HttpExchange;
import com.bolyartech.forge.base.exchange.ResultProducer;
import com.bolyartech.forge.base.exchange.builders.ForgePostHttpExchangeBuilder;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeHelper;
import com.bolyartech.forge.base.exchange.forge.ForgeExchangeResult;
import com.bolyartech.forge.base.rc_task.AbstractRcTask;
import com.bolyartech.forge.base.rc_task.RcTaskResult;
import com.bolyartech.forge.base.rc_task.RcTaskToExecutor;
import com.bolyartech.forge.skeleton.dagger.basic.app.SessionForgeExchangeExecutor;
import com.bolyartech.scram_sasl.client.ScramClientFunctionality;
import com.bolyartech.scram_sasl.common.ScramException;

import java.io.IOException;

import javax.inject.Inject;


public class ResRcTestImpl extends AbstractRctResidentComponent implements ResRcTest {
    private final ForgeExchangeHelper forgeExchangeHelper;
    private final ScramClientFunctionality scramClientFunctionality;
    private final SessionForgeExchangeExecutor sessionForgeExchangeExecutor;
    private Task1 task1;
    private Task2 task2;
    private RcTaskResult<String, Integer> task1result;
    private RcTaskResult<String, Void> task2result;


    @Inject
    public ResRcTestImpl(RcTaskExecutor taskExecutor,
                         ForgeExchangeHelper forgeExchangeHelper,
                         ScramClientFunctionality scramClientFunctionality, SessionForgeExchangeExecutor sessionForgeExchangeExecutor) {

        super(taskExecutor);
        this.forgeExchangeHelper = forgeExchangeHelper;
        this.scramClientFunctionality = scramClientFunctionality;
        this.sessionForgeExchangeExecutor = sessionForgeExchangeExecutor;
    }


    @Override
    public void test1() {
        task1 = new Task1();
        executeTask(task1);
    }


    @Override
    public void test2() {
        task2 = new Task2();
        executeTask(task2);
    }


    @Override
    public RcTaskResult<String, Integer> getTask1Result() {
        return task1result;
    }


    @Override
    public RcTaskResult<String, Void> getTask2Result() {
        return task2result;
    }


    @Override
    public synchronized void endedStateAcknowledged() {
        super.endedStateAcknowledged();

    }


    @Override
    protected void onTaskPostExecute(@NonNull RcTaskToExecutor task) {
        switch (getCurrentTask().getId()) {
            case 1:
                task1result = task1.getResult();
                break;
            case 2:
                task2result = task2.getResult();
                break;
            default:
                throw new AssertionError("Unexpected task ID");
        }
    }


    private static class Task2 extends AbstractRcTask<RcTaskResult<String, Void>> {

        private Task2() {
            super(2);
        }


        @Override
        public void execute() {
            try {
                Thread.sleep(2000);
                if (!isCancelled()) {
                    setTaskResult(RcTaskResult.createSuccessResult(null));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private class Task1 extends AbstractRcTask<RcTaskResult<String, Integer>> {
        private Task1() {
            super(1);
        }


        @Override
        public void execute() {
            ForgePostHttpExchangeBuilder step1builder = forgeExchangeHelper.createForgePostHttpExchangeBuilder("login");
            String clientFirst = null;
            try {
                clientFirst = scramClientFunctionality.prepareFirstMessage("ogre");
                step1builder.addPostParameter("app_type", "");
                step1builder.addPostParameter("app_version", "1");
                step1builder.addPostParameter("step", "1");
                step1builder.addPostParameter("data", clientFirst);
                HttpExchange<ForgeExchangeResult> x = step1builder.build();
                ForgeExchangeResult res = sessionForgeExchangeExecutor.execute(x);
                int i = 2;
                i++;

            } catch (ScramException e) {
                e.printStackTrace();
            } catch (ResultProducer.ResultProducerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (!isCancelled()) {
                setTaskResult(RcTaskResult.createErrorResult(12334));
            }
        }
    }
}
