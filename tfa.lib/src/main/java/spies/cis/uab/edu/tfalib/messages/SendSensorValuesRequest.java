package spies.cis.uab.edu.tfalib.messages;

/**
 * Created by Prakashs on 7/27/15.
 */
public class SendSensorValuesRequest extends Message {
    public static final short LEN = Header.LEN;
    public static final byte MSG_TYPE = 0x60;

    public SendSensorValuesRequest(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
    }

    public SendSensorValuesRequest() {
        super(MSG_TYPE, LEN, generateMsgId());
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
}
