package spies.cis.uab.edu.tfalib.threads;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.AudioValue;
import spies.cis.uab.edu.tfalib.bo.AudioValues;
import spies.cis.uab.edu.tfalib.messages.IMessage;
import spies.cis.uab.edu.tfalib.messages.AudioValuesMsg;
import spies.cis.uab.edu.tfalib.queues.AudioValuesQs;

/**
 * Created by Prakashs on 8/2/15.
 */
public class AudioValuesConsumer extends TwoFactorThread {
    private static final String LOG_TAG = AudioValuesConsumer.class
            .getSimpleName();

    private final AudioValuesQs sAudioValsQs;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;
    private final ExecutorService sExecutor = Executors.newFixedThreadPool(2);

    public AudioValuesConsumer(ThreadGroup tGroup, 
                               AudioValuesQs audioValsQs,
                               LinkedBlockingQueue<IMessage>outboundMsgQ){
        super(tGroup, LOG_TAG);
        this.sAudioValsQs = audioValsQs;
        this.sOutboundMsgQ=outboundMsgQ;
    }
    @Override
    public void interrupt() {
        super.interrupt();
        sExecutor.shutdown();
    }
    @Override
    protected void mainloop() throws InterruptedException {
        Log.d(LOG_TAG, "Started");

        List<AudioValue> overlappingVals = null;


        while (!isInterrupted()) {
            // Add audio values
            List<AudioValue> audioValueList = new ArrayList<AudioValue>();
            if(sAudioValsQs.getAudioValueQs().size() == 0){
                takeRest(100);
            }
            if (sAudioValsQs.getAudioValueQs().drainTo(audioValueList) > 0) {
                AudioValues audioValues = new AudioValues(audioValueList);
                sOutboundMsgQ.add(new AudioValuesMsg(audioValues));
            }
        }
        Log.d(LOG_TAG, "Finished");

    }
}
