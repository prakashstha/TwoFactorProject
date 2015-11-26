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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.ReceivedMsgBytes;

/**
 * 
 * @author Swapnil Udar
 *
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

		setPriority(Thread.MAX_PRIORITY);
		setDaemon(true);
	}

	@Override
	protected void mainloop() throws InterruptedException {
		byte[] reqStream = null;
		int numOfBytsRead = 0;
		int avail = 0;

		while (!isInterrupted()) {
			try {
                while(sBTInputStream == null){
                    takeRest(100);
                    continue;
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
				throw new RuntimeException(e);
			} catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
		}
	}
}