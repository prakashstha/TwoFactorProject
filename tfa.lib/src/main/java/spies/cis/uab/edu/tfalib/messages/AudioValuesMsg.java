package spies.cis.uab.edu.tfalib.messages;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import spies.cis.uab.edu.tfalib.bo.AudioValue;
import spies.cis.uab.edu.tfalib.bo.AudioValues;

/**
 * Created by prakashs on 7/31/2015.
 */
public class AudioValuesMsg extends Message{
    public static final byte MSG_TYPE = 0x15;

    private final AudioValues sAudioValues;

    public AudioValuesMsg(byte[] msg) {
        super(msg);
        // Check minimum length of audio value message
        if (msg == null || msg.length < (Header.LEN + AudioValue.LEN)) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        this.sAudioValues = new AudioValues(Arrays.copyOfRange(msg,
                Header.LEN, msg.length));
    }
    public AudioValuesMsg(AudioValues audioValues) {
        super(MSG_TYPE, calcMsgLen(audioValues), generateMsgId());
        this.sAudioValues = audioValues;
    }

    private static short calcMsgLen(AudioValues audioVals) {
        return (short) (Header.LEN + audioVals.getBytesLen());
    }
    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
    public AudioValues getsAudioValues() {
        return sAudioValues;
    }
    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(Header.LEN
                + sAudioValues.getBytesLen());
        bytBuffer.clear();
        bytBuffer.put(super.toByteArray());
        bytBuffer.put(sAudioValues.toByteArray());
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    @Override
    public String toString() {
        return sAudioValues.toString();
    }

}
