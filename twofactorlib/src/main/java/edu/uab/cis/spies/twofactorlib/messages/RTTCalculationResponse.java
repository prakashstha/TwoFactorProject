
package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * <p>
 *     Round Trip Time Calculation Response Message
 * </p>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class RTTCalculationResponse extends Message {
	public static final short LEN = Header.LEN + 8;
	public static final byte MSG_TYPE = 0x01;

	private final long sRequesterTimestamp;

	public RTTCalculationResponse(byte[] msg) {
		super(msg);
		if (msg.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
		sRequesterTimestamp = ConversionUtil.conv2Long(Arrays.copyOfRange(msg,
                Header.LEN, LEN));
	}

	public RTTCalculationResponse(IMessage req) {
		super(MSG_TYPE, LEN, req.getMsgId());
		// Check instance of request
		if (!(req instanceof RTTCalculationRequest)) {
			throw new IllegalArgumentException("Incorrect request");
		}
		sRequesterTimestamp = ((RTTCalculationRequest) req).getReqTimestamp();
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
		msgBytArray.put(super.toByteArray());
		msgBytArray.putLong(sRequesterTimestamp);
		msgBytArray.order(ByteOrder.BIG_ENDIAN);
		return msgBytArray.array();
	}

	public long getRequesterTimestmp() {
		return sRequesterTimestamp;
	}

	@Override
	public void validateMsgType() {
		if (getMsgType() != MSG_TYPE) {
			throw new IllegalArgumentException("Incorrect msg type");
		}
	}
}