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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.ReceivedMsgBytes;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;
import spies.cis.uab.edu.tfalib.messages.IMessage;
import spies.cis.uab.edu.tfalib.messages.MessageFactory;
import spies.cis.uab.edu.tfalib.messages.handler.IMessageHandlerFactory;

/**
 * 
 * @author Swapnil Udar
 *
 */
public class ReceivedMsgHandler extends TwoFactorThread {

	private static final String LOG_TAG = ReceivedMsgHandler.class
			.getSimpleName();

	private final IMessageHandlerFactory sHandlerFactory;
	private final LinkedBlockingQueue<ReceivedMsgBytes> sMsgProcessorQ;

	public ReceivedMsgHandler(ThreadGroup tGroup,
			IMessageHandlerFactory handlerFactory,
			LinkedBlockingQueue<ReceivedMsgBytes> msgProcessorQ) {
		super(tGroup, LOG_TAG);
		this.sHandlerFactory = handlerFactory;
		this.sMsgProcessorQ = msgProcessorQ;
	}

	@Override
	protected void mainloop() throws InterruptedException {
		ReceivedMsgBytes rcvdBytes = null;
		byte[] prevUnusedBytes = null;
		List<IMessage> msgs;

		while (!isInterrupted()) {
			if (sMsgProcessorQ.isEmpty()) {
				takeRest(10);
				continue;
			}

			rcvdBytes = sMsgProcessorQ.remove();
			/*System.out.println("Message bytes: "
					+ MessageFactory.formatHexBytes(Arrays.copyOfRange(
							rcvdBytes.getBytes(), 0, rcvdBytes.getLen())));*/

			// Append previously unused bytes
			rcvdBytes = append2PrevUnusedBytes(prevUnusedBytes, rcvdBytes);
			/*System.out.println("Appended bytes: "
					+ MessageFactory.formatHexBytes(Arrays.copyOfRange(
							rcvdBytes.getBytes(), 0, rcvdBytes.getLen())));*/

			// It is assumed that first bytes of rcvdBytes always a message
			// type, if it is not then it will fail for sure.
			int expectedMsgSize = ConversionUtil.conv2Short(new byte[]{
                    rcvdBytes.getBytes()[1], rcvdBytes.getBytes()[2]});
			if (expectedMsgSize <= rcvdBytes.getLen()) {
				// Parse byte stream to message instance
 				msgs = MessageFactory.getMessages(Arrays.copyOfRange(
                        rcvdBytes.getBytes(), 0, rcvdBytes.getLen()));

				// Handle message
				for (IMessage msg : msgs) {
                    //Log.e(LOG_TAG, "received message type" + msg.getMsgType());
                    msg.handle(sHandlerFactory.getHandler(msg));
				}
				prevUnusedBytes = getUnusedBytes(rcvdBytes, msgs);
				/*if (prevUnusedBytes != null && prevUnusedBytes.length > 0) {
					System.out.println("Unused bytes: "
							+ MessageFactory.formatHexBytes(prevUnusedBytes));
				}*/
			} else {
				prevUnusedBytes = Arrays.copyOfRange(rcvdBytes.getBytes(), 0,
						rcvdBytes.getLen());
				/*if (prevUnusedBytes != null && prevUnusedBytes.length > 0) {
					System.out.println("Unused bytes: "
							+ MessageFactory.formatHexBytes(prevUnusedBytes));
				}*/
			}
		}
	}

	/**
	 * Check if there are any unused bytes from previous execution. If there are
	 * then the recently received bytes are appended to it.
	 * 
	 * @param prevUnusedBytes
	 * @param rcvdBytes
	 * @return
	 */
	private ReceivedMsgBytes append2PrevUnusedBytes(byte[] prevUnusedBytes,
			ReceivedMsgBytes rcvdBytes) {
		// If previous unused bytes are null nothing need to be appended
		if (prevUnusedBytes == null) {
			return rcvdBytes;
		}
		byte[] appendedByts = new byte[prevUnusedBytes.length
				+ rcvdBytes.getLen()];
		System.arraycopy(prevUnusedBytes, 0, appendedByts, 0,
				prevUnusedBytes.length);
		System.arraycopy(rcvdBytes.getBytes(), 0, appendedByts,
				prevUnusedBytes.length, rcvdBytes.getLen());
		return new ReceivedMsgBytes(appendedByts, prevUnusedBytes.length
				+ rcvdBytes.getLen());
	}

	/**
	 * Get bytes from the received bytes, which are used for converting them
	 * into a valid messages.
	 * 
	 */
	private byte[] getUnusedBytes(ReceivedMsgBytes rcvdBytes,
			List<IMessage> msgs) {
		int lenOfMsgs = calcLenOfMsgs(msgs);
		// All the received bytes are converted into legitimate messages.
		if (rcvdBytes.getLen() == lenOfMsgs) {
			return null;
		}
		if (lenOfMsgs > rcvdBytes.getLen()) {
			throw new IllegalArgumentException(
					"length of received bytes is less than corresponding messages");
		}
		return Arrays.copyOfRange(rcvdBytes.getBytes(), lenOfMsgs,
				rcvdBytes.getLen());
	}

	private int calcLenOfMsgs(List<IMessage> msgs) {
		int len = 0;
		for (IMessage msg : msgs) {
			len = len + msg.getMsgLen();
		}
		return len;
	}
}