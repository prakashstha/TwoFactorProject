
package edu.uab.cis.spies.twofactorlib.messages;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class StopResponse extends Message {
    public static final short LEN = Header.LEN;
    public static final byte MSG_TYPE = 0x51;

    public StopResponse(byte[] msg) {
        super(msg);
        if (msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
    }

    public StopResponse(IMessage req) {
        super(MSG_TYPE, LEN, req.getMsgId());
        // Check instance of request
        if (!(req instanceof StopRequest)) {
            throw new IllegalArgumentException("Incorrect request");
        }
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }
}