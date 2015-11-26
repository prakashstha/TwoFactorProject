
package edu.uab.cis.spies.twofactorlib.threads;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.ReceivedMsgBytes;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class MessageReceiver extends TwoFactorThread {

    private static final String LOG_TAG = MessageReceiver.class.getSimpleName();

    private static final int MAX_NUM_BYTES = 100000;

    private final InputStream sBTInputStream;
    private final LinkedBlockingQueue<ReceivedMsgBytes> sMsgProcessorQ;

    public MessageReceiver(ThreadGroup tGroup, InputStream btInputStream,
                           LinkedBlockingQueue<ReceivedMsgBytes> msgProcessorQ) {
        super(tGroup, LOG_TAG);
        this.sBTInputStream = btInputStream;
        this.sMsgProcessorQ = msgProcessorQ;

        setPriority(Thread.NORM_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG,ThreadStatus.INTERRUPTED);
    }

    @Override
    protected void mainloop() {

        Log.d(LOG_TAG, ThreadStatus.START);

        byte[] reqStream = null;
        int numOfBytsRead = 0;
        int avail = 0;

        Log.d(LOG_TAG, ThreadStatus.RUNNING);
        while (!isInterrupted()) {
            try {
                if(sBTInputStream == null){
                    throw new RuntimeException("Bluetooth InputChannel is null.");
                }
                avail = sBTInputStream.available();
                // Check if there are bytes available in the input stream
                if (avail <= 0) {
                    takeRest(100);
                    continue;
                }

                reqStream = new byte[MAX_NUM_BYTES];
                numOfBytsRead = sBTInputStream.read(reqStream);
                //Log.e(LOG_TAG, "REceivcd byete: "+numOfBytsRead);
                if (numOfBytsRead == 0) {
                    continue;
                }

                sMsgProcessorQ.add(new ReceivedMsgBytes(reqStream,
                        numOfBytsRead));
            } catch (EOFException e) {
                Log.e(LOG_TAG, ThreadStatus.EXCEPTION);
                throw new RuntimeException(e);
            } catch (IOException ioe) {
                Log.e(LOG_TAG,ThreadStatus.EXCEPTION);
                throw new RuntimeException(ioe);
            }catch(InterruptedException ex){
                Log.d(LOG_TAG, ThreadStatus.INTERRUPTED + "While taking rest.");
            }
        }

        Log.d(LOG_TAG, ThreadStatus.END);
    }
}