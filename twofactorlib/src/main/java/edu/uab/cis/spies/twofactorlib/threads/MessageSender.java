
package edu.uab.cis.spies.twofactorlib.threads;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.Message;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class MessageSender extends TwoFactorThread {
    private static final String LOG_TAG = MessageSender.class.getSimpleName();

    private final OutputStream sBTOutputStream;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;

    public MessageSender(ThreadGroup tGroup, OutputStream btOutputStream,
                         LinkedBlockingQueue<IMessage> outboundMsgQ) {
        super(tGroup, LOG_TAG);
        this.sBTOutputStream = btOutputStream;
        this.sOutboundMsgQ = outboundMsgQ;
        setPriority(Thread.NORM_PRIORITY);
        setDaemon(true);
    }

    @Override
    protected void mainloop() {
        Log.e(LOG_TAG, ThreadStatus.START);
        IMessage[] outbondMsgs = null;
        List<IMessage> messages = null;
        byte[] byteStream = null;
        Log.d(LOG_TAG,ThreadStatus.RUNNING);
        while (true) {
            if (sBTOutputStream == null) {
               throw new RuntimeException("Bluetooth output stream is null.");
            }
            if (sOutboundMsgQ.isEmpty()) {
                try {
                    takeRest(100);
                } catch (InterruptedException e) {
                    Log.d(LOG_TAG,ThreadStatus.INTERRUPTED + "While taking rest.");
                }
                if(isInterrupted()){
                    break;
                }else{
                    continue;
                }
            }
            messages = new ArrayList<IMessage>();

            if (sOutboundMsgQ.drainTo(messages) > 0) {
                outbondMsgs = messages.toArray(new IMessage[messages.size()]);

//			for (IMessage tmp : outbondMsgs) {
//					System.out.println("Sending message of type : "
//							+ tmp.getClass().getSimpleName());
//				}

                byteStream = Message.toByteArray(outbondMsgs);
                try {
                    sBTOutputStream.write(byteStream);
                    sBTOutputStream.flush();
                } catch (IOException ioe) {
                    Log.d(LOG_TAG,ThreadStatus.EXCEPTION);
                    throw new RuntimeException(ioe);
                }
            }
        }
    }
}