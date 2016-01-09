package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;


/**
 * <p>
 *     Each message has a header that holds type of message,
 *     message length and message id information.
 *</p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class Header {
    public static final short LEN = 7;

    private final byte sType;
    private final short sLen;
    private final int sMsgId;

    /**
     * Converts byte stream representation of <code>Header</code> to <code>Header<code/>
     * @param msg byte stream representation of Header
     *
     */
    public Header(byte[] msg) {
        super();
        if (msg == null || msg.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        this.sType = msg[0];
        this.sLen = ConversionUtil.conv2Short(Arrays.copyOfRange(msg, 1, 3));
        this.sMsgId = ConversionUtil.conv2Int(Arrays.copyOfRange(msg, 3, LEN));
    }

    /**
     * create Header from <code>type, len, msgId</code>
     *
     * @param type type of message
     * @param len length of message in bytes including header
     * @param msgId unique id of message
     */
    public Header(byte type, short len, int msgId) {
        super();
        this.sType = type;
        this.sLen = len;
        this.sMsgId = msgId;
    }

    public byte getMsgType() {
        return sType;
    }

    public short getMsgLen() {
        return sLen;
    }

    public int getMsgId() {
        return sMsgId;
    }

    /**
     * convert to byte stream
     * @return byte stream representation of <code>Header</code>
     */
    public byte[] toByteArray() {
        ByteBuffer msgBytArray = ByteBuffer.allocate(LEN);
        msgBytArray.put(sType);
        msgBytArray.putShort(sLen);
        msgBytArray.putInt(sMsgId);
        msgBytArray.order(ByteOrder.BIG_ENDIAN);
        return msgBytArray.array();
    }
}