package edu.uab.cis.spies.twofactorlib.messages;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class WarningMsg extends Message {
    public static final short LEN = Header.LEN;
    public static final byte MSG_TYPE = 0x04;

    public WarningMsg(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
    }

    public WarningMsg() {
        super(MSG_TYPE, LEN, generateMsgId());
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }

}