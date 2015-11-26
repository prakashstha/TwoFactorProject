
package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.bo.SensorValue;
import edu.uab.cis.spies.twofactorlib.bo.SensorValues;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValuesMsg extends Message {
    public static final byte MSG_TYPE = 0x40;

    private final SensorValues sSensorValues;

    public SensorValuesMsg(byte[] msg) {
        super(msg);
        // Check minimum length of sensor value message
        if (msg == null || msg.length < (Header.LEN + SensorValue.LEN)) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        this.sSensorValues = new SensorValues(Arrays.copyOfRange(msg,
                Header.LEN, msg.length));
    }

    public SensorValuesMsg(SensorValues sensorVals) {
        super(MSG_TYPE, calcMsgLen(sensorVals), generateMsgId());
        this.sSensorValues = sensorVals;
    }

    private static short calcMsgLen(SensorValues sensorVals) {
        return (short) (Header.LEN + sensorVals.getBytesLen());
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }

    public SensorValues getSensorValues() {
        return sSensorValues;
    }

    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(Header.LEN
                + sSensorValues.getBytesLen());
        bytBuffer.clear();
        bytBuffer.put(super.toByteArray());
        bytBuffer.put(sSensorValues.toByteArray());
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    @Override
    public String toString() {
        return sSensorValues.toString();
    }
}