
package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * <p>
 *     Round Trip Time Calculation Request Message
 * </p>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class RTTCalculationRequest extends Message {
	public static final short LEN = Header.LEN + 8;
	public static final byte MSG_TYPE = 0x00;

	private final long sReqTimestamp;

	public RTTCalculationRequest(byte[] msg) {
		super(msg);
		if (msg.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
		sReqTimestamp = ConversionUtil.conv2Long(Arrays.copyOfRange(msg,
                Header.LEN, LEN));
	}

	public RTTCalculationRequest() {
		super(MSG_TYPE, LEN, generateMsgId());
		sReqTimestamp = System.currentTimeMillis();
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
		msgBytArray.put(super.toByteArray());
		msgBytArray.putLong(sReqTimestamp);
		msgBytArray.order(ByteOrder.BIG_ENDIAN);
		return msgBytArray.array();
	}

	public long getReqTimestamp() {
		return sReqTimestamp;
	}

	@Override
	public void validateMsgType() {
		if (getMsgType() != MSG_TYPE) {
			throw new IllegalArgumentException("Incorrect msg type");
		}
	}
}