package edu.uab.cis.spies.twofactorlib.threads;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.AudioValue;
import edu.uab.cis.spies.twofactorlib.bo.AudioValues;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.messages.AudioValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.queues.AudioValuesQs;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioValuesConsumer extends TwoFactorThread {
    private static final String LOG_TAG = AudioValuesConsumer.class
            .getSimpleName();

    private final AudioValuesQs sAudioValsQs;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;
    private final ExecutorService sExecutor = Executors.newFixedThreadPool(2);

    public AudioValuesConsumer(ThreadGroup tGroup,
                               AudioValuesQs audioValsQs,
                               LinkedBlockingQueue<IMessage> outboundMsgQ) {
        super(tGroup, LOG_TAG);
        this.sAudioValsQs = audioValsQs;
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
        List<AudioValue> overlappingVals = null;
        Log.d(LOG_TAG, ThreadStatus.RUNNING);
        while (true) {
            // Add audio values
            List<AudioValue> audioValueList = new ArrayList<AudioValue>();
            if (sAudioValsQs.getAudioValueQs().size() == 0) {
                try {
                    takeRest(100);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG, ThreadStatus.INTERRUPTED + "while taking rest");
                }
            }
            if (sAudioValsQs.getAudioValueQs().drainTo(audioValueList) > 0) {
                //AudioValues audioValues = new AudioValues(audioValueList);
                sOutboundMsgQ.add(new AudioValuesMsg(new AudioValues(audioValueList)));
            }
            if(isInterrupted() && sAudioValsQs.getAudioValueQs().isEmpty())
                break;
        }
        Log.d(LOG_TAG, ThreadStatus.END);

    }
}
