package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * <p>
 *     Message that holds of audio start time
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioStartTimeInfoMsg extends Message{
    public static final short LEN = Header.LEN + Constants.LONG_SIZE;
    public static final byte MSG_TYPE = 0x18;
    public final long startTimestamp;

    /**
     * converts byte stream of message to AudioStartTimeInfoMsg
     * @param msg
     *      byte stream of message.
     */
    public AudioStartTimeInfoMsg(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        int start = Header.LEN;
        int end = Header.LEN + Constants.LONG_SIZE;
        startTimestamp = ConversionUtil.conv2Long(Arrays.copyOfRange(msg, start,
                end));
//        start = end;
//        end = start + Constants.LONG_SIZE;
//        endTimestamp = ConversionUtil.conv2Long(Arrays.copyOfRange(msg, start,
//                end));
    }

    /**
     * create a AudioStartTimeInfoMsg with given timestamp
     * @param startTimestamp
     *          Audio start Time-stamp
     */
    public AudioStartTimeInfoMsg(long startTimestamp){
        super(MSG_TYPE, LEN, generateMsgId());
        this.startTimestamp = startTimestamp;
    }


    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
    @Override
    public byte[] toByteArray() {
        ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
        msgBytArray.put(super.toByteArray());
        msgBytArray.putLong(startTimestamp);
        msgBytArray.order(ByteOrder.BIG_ENDIAN);
        return msgBytArray.array();
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

}
