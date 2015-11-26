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

package spies.cis.uab.edu.tfalib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import spies.cis.uab.edu.tfalib.common.ConversionUtil;

/**
 * 
 * @author Swapnil Udar
 *
 */
public class Header {
	public static final short LEN = 7;

	private final byte sType;
	private final short sLen;
	private final int sMsgId;

	public Header(byte[] msg) {
		super();
		if (msg == null || msg.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
		this.sType = msg[0];
		this.sLen = ConversionUtil.conv2Short(Arrays.copyOfRange(msg, 1, 3));
		this.sMsgId = ConversionUtil.conv2Int(Arrays.copyOfRange(msg, 3, LEN));
	}

	public Header(byte type, short len, int msgId) {
		super();
		this.sType = type;
		this.sLen = len;
		this.sMsgId = msgId;
	}

	public byte getMsgType() {
		return sType;
	}

	public short getMsgLen() {
		return sLen;
	}

	public int getMsgId() {
		return sMsgId;
	}

	public byte[] toByteArray() {
		ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
		msgBytArray.put(sType);
		msgBytArray.putShort(sLen);
		msgBytArray.putInt(sMsgId);
		msgBytArray.order(ByteOrder.BIG_ENDIAN);
		return msgBytArray.array();
	}
}