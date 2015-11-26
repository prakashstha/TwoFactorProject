package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.AudioParameters;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SendAudioValuesResponse extends Message {
    public static final short LEN = Header.LEN + Constants.SHORT_SIZE;
    public static final byte MSG_TYPE = 0x17;
    private final short bufferSize;

    public SendAudioValuesResponse(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        bufferSize = ConversionUtil.conv2Short(Arrays.copyOfRange(msg, Header.LEN, LEN));
    }

    public SendAudioValuesResponse(IMessage req) {
        super(MSG_TYPE, LEN, req.getMsgId());
        // Check instance of request
        if (!(req instanceof SendAudioValuesRequest)) {
            throw new IllegalArgumentException("Incorrect request");
        }
        bufferSize = AudioParameters.bufferSize;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
        msgBytArray.put(super.toByteArray());
        msgBytArray.putShort(bufferSize);
        msgBytArray.order(ByteOrder.BIG_ENDIAN);
        return msgBytArray.array();
    }

    public long getTime() {
        return bufferSize;
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
}
