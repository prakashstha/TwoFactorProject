package spies.cis.uab.edu.tfalib.threads;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.SensorValue;
import spies.cis.uab.edu.tfalib.bo.SensorValues;
import spies.cis.uab.edu.tfalib.messages.IMessage;
import spies.cis.uab.edu.tfalib.messages.SensorValuesMsg;
import spies.cis.uab.edu.tfalib.queues.SensorValuesQs;

/**
 * Created by Prakashs on 7/27/15.
 */
public class SensorValuesSegmenter extends TwoFactorThread {
    private static final String LOG_TAG = SensorValuesSegmenter.class
            .getSimpleName();

    private final SensorValuesQs sSensorValsQs;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;
    private final ExecutorService sExecutor = Executors.newFixedThreadPool(2);

    public SensorValuesSegmenter(ThreadGroup tGroup,
                                 SensorValuesQs sensorValuesQ,
                                 LinkedBlockingQueue<IMessage> outboundMsgQ
                                 ) {
        super(tGroup, LOG_TAG);
        this.sSensorValsQs = sensorValuesQ;
        this.sOutboundMsgQ = outboundMsgQ;
    }



    @Override
    public void interrupt() {
        super.interrupt();
        sExecutor.shutdown();
    }

    @Override
    protected void mainloop() throws InterruptedException {
        Log.d(LOG_TAG, "Started");

        List<SensorValue> overlappingVals = null;


        while (!isInterrupted()) {
            // Add accelerometer sensor values
                List<SensorValue> sensorValueList = new ArrayList<SensorValue>();
                if (sSensorValsQs.getAccSegmentersQ().drainTo(sensorValueList) > 0) {
                    SensorValues sensorValues = new SensorValues(
                            sensorValueList);
                    sOutboundMsgQ.add(new SensorValuesMsg(sensorValues));
                }

                // Add gyroscope sensor values
                sensorValueList = new ArrayList<SensorValue>();
                if (sSensorValsQs.getGyroSegmentersQ().drainTo(sensorValueList) > 0) {
                    SensorValues sensorValues = new SensorValues(
                            sensorValueList);
                    sOutboundMsgQ.add(new SensorValuesMsg(sensorValues));
                }

        }
        Log.d(LOG_TAG, "Finished");
    }


}
