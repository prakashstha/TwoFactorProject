package edu.uab.cis.spies.twofactorlib.threads;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.SensorValue;
import edu.uab.cis.spies.twofactorlib.bo.SensorValues;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.SensorValuesMsg;
import edu.uab.cis.spies.twofactorlib.queues.SensorValuesQs;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValuesConsumer extends TwoFactorThread {
    private static final String LOG_TAG = SensorValuesConsumer.class
            .getSimpleName();

    private final SensorValuesQs sSensorValsQs;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;
    private final ExecutorService sExecutor = Executors.newFixedThreadPool(2);

    public SensorValuesConsumer(ThreadGroup tGroup,
                                SensorValuesQs sensorValuesQ,
                                LinkedBlockingQueue<IMessage> outboundMsgQ
    ) {
        super(tGroup, LOG_TAG);
        this.sSensorValsQs = sensorValuesQ;
        this.sOutboundMsgQ = outboundMsgQ;
        setPriority(Thread.NORM_PRIORITY);
    }


    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG,ThreadStatus.INTERRUPTED);
        sExecutor.shutdown();
    }

    @Override
    protected void mainloop() {
        Log.d(LOG_TAG, ThreadStatus.START);
        Log.d(LOG_TAG,ThreadStatus.RUNNING);
        while (true) {
            if(sSensorValsQs.getAccSegmentersQ().isEmpty() && sSensorValsQs.getGyroSegmentersQ().isEmpty()){
                try {
                    takeRest(100);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG,ThreadStatus.INTERRUPTED + "While waiting.");
                }
                if(isInterrupted()){
                    break;
                }
                continue;
            }
            // Add accelerometer sensor values
            List<SensorValue> sensorValueList = new ArrayList<SensorValue>();
            if (sSensorValsQs.getAccSegmentersQ().drainTo(sensorValueList) > 0) {
                //SensorValues sensorValues = new SensorValues(sensorValueList);
                sOutboundMsgQ.add(new SensorValuesMsg(new SensorValues(sensorValueList)));
            }

            // Add gyroscope sensor values
            sensorValueList = new ArrayList<SensorValue>();
            if (sSensorValsQs.getGyroSegmentersQ().drainTo(sensorValueList) > 0) {
               // SensorValues sensorValues = new SensorValues(sensorValueList);
                sOutboundMsgQ.add(new SensorValuesMsg(new SensorValues(sensorValueList)));
            }

        }
        Log.d(LOG_TAG, ThreadStatus.END);
    }


}
