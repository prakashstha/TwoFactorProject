
package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;


/**
 * 
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class ShareTimeResponse extends Message {
	public static final short LEN = Header.LEN + Constants.LONG_SIZE + Constants.LONG_SIZE;
	public static final byte MSG_TYPE = 0x31;

	private final long sTime;
    private final long sRequesterTimestamp;

	public ShareTimeResponse(byte[] msg) {
		super(msg);
		if (msg.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
        int start = Header.LEN;
        int end = Header.LEN + Constants.LONG_SIZE;
        sTime = ConversionUtil.conv2Long(Arrays.copyOfRange(msg, start,
                end));
        start = end;
        end = start + Constants.LONG_SIZE;
        sRequesterTimestamp = ConversionUtil.conv2Long(Arrays.copyOfRange(msg, start,
                end));
	}

	public ShareTimeResponse(IMessage req) {
		super(MSG_TYPE, LEN, req.getMsgId());
		// Check instance of request
		if (!(req instanceof ShareTimeRequest)) {
			throw new IllegalArgumentException("Incorrect request");
		}
        sTime = System.currentTimeMillis();
        sRequesterTimestamp = ((ShareTimeRequest) req).getReqTimestamp();
	}

	@Override
	public byte[] toByteArray() {
		ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
		msgBytArray.put(super.toByteArray());
		msgBytArray.putLong(sTime);
        msgBytArray.putLong(sRequesterTimestamp);
		msgBytArray.order(ByteOrder.BIG_ENDIAN);
		return msgBytArray.array();
	}

    public long getServerTime() {
        return sTime;
    }

    public long getsRequesterTimestamp(){
        return sRequesterTimestamp;
    }
	@Override
	public void validateMsgType() {
		if (getMsgType() != MSG_TYPE) {
			throw new IllegalArgumentException("Incorrect msg type");
		}
	}
}