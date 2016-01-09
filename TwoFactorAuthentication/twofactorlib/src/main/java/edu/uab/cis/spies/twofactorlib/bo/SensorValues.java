package edu.uab.cis.spies.twofactorlib.bo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

import static java.util.Arrays.copyOfRange;
import static edu.uab.cis.spies.twofactorlib.common.Constants.SHORT_SIZE;

/**
 * Holds <code>List<SensorValue></code>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValues {
    private final List<SensorValue> sSensorValues;

    /**
     * convert raw byte stream to SensorValues
     * @param msg
     *      raw byte stream of SensorValues
     */
    public SensorValues(byte[] msg) {
        super();
        this.sSensorValues = convert(msg);
    }

    public SensorValues(SensorValue[] sensorVals) {
        super();
        this.sSensorValues = new ArrayList<SensorValue>(
                Arrays.asList(sensorVals));
    }

    public SensorValues() {
        super();
        this.sSensorValues = new ArrayList<SensorValue>();
    }

    public SensorValues(List<SensorValue> sensorVals) {
        this(sensorVals.toArray(new SensorValue[sensorVals.size()]));
    }

    /**
     * Get the bytes length of SensorValues
     * Bytes length of SensorValues = bytes for [holding number of sensor values] + number of bytes of SensorValues
     * @return
     *      bytes length of SensorValues
     */
    public int getBytesLen() {
        return (short) (Constants.SHORT_SIZE + (SensorValue.LEN * sSensorValues
                .size()));
    }

    /**
     * Add SensorValue to the list
     * @param sensorVal
     *      SensorValue to be added in the list
     */
    public void add(SensorValue sensorVal) {
        synchronized (this) {
            sSensorValues.add(sensorVal);
        }
    }

    /**
     * Convert SensorValues to list of SensorValue
     * @return
     *      list of SensorValue
     */
    public List<SensorValue> asList() {
        return sSensorValues;
    }

    /**
     * Convert SensorValues to Array
     * @return
     *      SensorValues as array
     */
    public SensorValue[] asArray() {
        return sSensorValues.toArray(new SensorValue[sSensorValues.size()]);
    }

    /**
     * Convert SensorValues to array of values where i+3, (i+1) + 3, (i+2) + 3
     * indexed values in array are <code>sValue_x, sValue_y, sValue_z</code> respectively
     *
     * @return
     */
    public float[] asValues() {
        float[] values = new float[sSensorValues.size()*3];
        for (int i = 0; i < sSensorValues.size(); i+=3) {
            float[] val = sSensorValues.get(i).getValue();
            values[i] = val[0];
            values[i+1] = val[1];
            values[i+2] = val[2];
        }
        return values;
    }

    /**
     * Clear the list
     */
    public void clear() {
        sSensorValues.clear();
    }

    /**
     * Conver SensorValues to raw byte stream
     * @return
     *      raw byte stream of SensorValues
     */
    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(getBytesLen());
        bytBuffer.clear();
        bytBuffer.putShort((short) sSensorValues.size());
        for (SensorValue sensorVal : sSensorValues) {
            bytBuffer.put(sensorVal.toByteArray());
        }
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    /**
     * Converts raw byte stream to list of SensorValue
     * @param rawByts
     *          raw byte stream of SensorValues
     * @return
     *    List of SensorValue
     */
    private List<SensorValue> convert(byte[] rawByts) {
        if (!isNumOfBytsValid(rawByts)) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        short size = ConversionUtil.conv2Short(copyOfRange(rawByts, 0, 2));
        List<SensorValue> sensorVals = new ArrayList<SensorValue>(size);
        int startIndex = 0;
        int endIndex = 2;
        for (int i = 0; i < size; i++) {
            startIndex = endIndex;
            endIndex = startIndex + SensorValue.LEN;
            sensorVals.add(new SensorValue(Arrays.copyOfRange(rawByts,
                    startIndex, endIndex)));
        }
        return sensorVals;
    }

    /**
     *
     * @param rawByts
     *      raw byte stream of SensorValues
     * @return
     *      <code>True<code/> if size of raw byte arrays is valid
     */
    private boolean isNumOfBytsValid(byte[] rawByts) {
        if (rawByts == null || rawByts.length == 0) {
            return false;
        }
        int len = rawByts.length;
        if (len > 0 && len < (SensorValue.LEN + SHORT_SIZE)) {
            return false;
        }
        int size = ByteBuffer.wrap(Arrays.copyOfRange(rawByts, 0, 2))
                .order(ByteOrder.BIG_ENDIAN).getShort();
        if ((size < 0) || (len < ((size * SensorValue.LEN) + 2))) {
            return false;
        }
        return true;
    }

    public String format() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuffer toString = new StringBuffer();
        for (SensorValue sensorVal : sSensorValues) {
            toString.append("\n").append(sensorVal.toString());
        }
        return toString.toString();
    }
}