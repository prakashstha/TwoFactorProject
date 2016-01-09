package edu.uab.cis.spies.twofactorlib.messages;

/**
 * Created by prakashs on 8/16/2015.
 */
public class SendAudioValuesRequest extends Message{
    public static final short LEN = Header.LEN;
    public static final byte MSG_TYPE = 0x16;

    public SendAudioValuesRequest(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
    }

    public SendAudioValuesRequest() {
        super(MSG_TYPE, LEN, generateMsgId());
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
}
