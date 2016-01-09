/*
 * Copyright 2015 (c) Secure System Group (https://se-sy.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spies.cis.uab.edu.tfalib.threads;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.messages.IMessage;
import spies.cis.uab.edu.tfalib.messages.Message;

/**
 * 
 * @author Swapnil Udar
 *
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
		setPriority(Thread.MAX_PRIORITY);
		setDaemon(true);
	}

	@Override
	protected void mainloop() throws InterruptedException {
        Log.e(LOG_TAG,"mainloop");
		IMessage[] outbondMsgs = null;
		List<IMessage> messages = null;
        byte[] byteStream = null;
		while (!isInterrupted()) {
            if(sBTOutputStream == null)
            {
                Log.e(LOG_TAG, "sBTOutputStream>>NULL");
                break;
            }
            if (sOutboundMsgQ.isEmpty()) {

                takeRest(10);
                continue;
            }
			messages = new ArrayList<IMessage>();

			if (sOutboundMsgQ.drainTo(messages) > 0) {
                //Log.e(LOG_TAG,"message size in thread: " + messages.size());
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
					throw new RuntimeException(ioe);
				}
			}
		}
	}
}