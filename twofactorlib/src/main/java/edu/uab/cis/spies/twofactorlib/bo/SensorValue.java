package edu.uab.cis.spies.twofactorlib.bo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;
import edu.uab.cis.spies.twofactorlib.enumerations.SensorTypes;

/**
 * Holds sensor values
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValue {
    public static final short LEN = Constants.FLOAT_SZ * 3 + Constants.BYTE_SIZE
            + Constants.LONG_SIZE;

    protected final float sValue_x;
    protected final float sValue_y;
    protected final float sValue_z;
    protected final byte sSensorId;
    protected long sEventTime;

    /**
     *
     * @param values
     *          array of sensor values
     * @param sensorId
     *          each sensor is given unique id
     *          <code>ACCELROMETER = 0x01;</code>
     *          <code>GYROSCOPE = 0x02;</code>
     * @param eventTmsp
     *          event time-stamp in millisecond
     */
    public SensorValue(float values[], byte sensorId, long eventTmsp) {
        super();
        this.sValue_x = values[0];
        this.sValue_y = values[1];
        this.sValue_z = values[2];
        this.sSensorId = sensorId;
        this.sEventTime = eventTmsp;
    }

    /**
     * converts raw byte stream to corresponding SensorValue
     * @param byts
     *      raw bytes that have <code>sValue_x, sValue_y, sValue_z, sEventTime, and sSensorId</code>
     *      in sequence
     */
    public SensorValue(byte[] byts) {

        if (byts.length != LEN) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }

        int start = 0;
        int end = start + Constants.FLOAT_SZ;
        sValue_x = ConversionUtil.conv2Float(Arrays.copyOfRange(byts, start,end));

        start = end;
        end = end + Constants.FLOAT_SZ;
        sValue_y = ConversionUtil.conv2Float(Arrays.copyOfRange(byts, start,end));

        start = end;
        end = end + Constants.FLOAT_SZ;
        sValue_z = ConversionUtil.conv2Float(Arrays.copyOfRange(byts, start,end));

        start = end;
        end = start + Constants.LONG_SIZE;
        sEventTime = ConversionUtil.conv2Long(Arrays.copyOfRange(byts, start,
                end));

        sSensorId = byts[end];
    }

    /**
     * Used when creating instance for testing purpose
     */
    public SensorValue(String sensorValue) {
        super();

        if (sensorValue == null || sensorValue.trim().length() == 0) {
            throw new IllegalArgumentException("Incorrect message string");
        }

        String[] split = sensorValue.split(",");
        if (split.length != 5) {
            throw new IllegalArgumentException("Incorrect message string");
        }
        this.sEventTime = Long.valueOf(split[0]);
        this.sSensorId = Byte.valueOf(split[1]);
        this.sValue_x = Float.valueOf(split[2]);
        this.sValue_y = Float.valueOf(split[3]);
        this.sValue_z = Float.valueOf(split[4]);
    }

    public SensorTypes getSensorType() {
        return SensorTypes.getSensorType(sSensorId);
    }

    public byte getSensorId() {
        return sSensorId;
    }

    public float[] getValue() {
        float[] sValue = {sValue_x, sValue_y, sValue_z};
        return sValue;
    }


    public long getTime() {
        return sEventTime;
    }


    /**
     * convert SensorValue to raw byte stream. Here <code>sValue_x, sValue_y, sValue_z,
     * sEventTime, sSensorId</code> are organized in BIG_INDIAN order
     *
     * @return
     *      raw byte stream representation of SensorValue.
     *
     */
    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(LEN);
        bytBuffer.putFloat(sValue_x);
        bytBuffer.putFloat(sValue_y);
        bytBuffer.putFloat(sValue_z);
        bytBuffer.putLong(sEventTime);
        bytBuffer.put(sSensorId);
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    public String formatAcc() {
        return String.format(Locale.getDefault(), "%d,%d,%s", sEventTime,
                sSensorId, fmtVal(sValue_x, sValue_y, sValue_z));
    }

    public String formatGyro() {
        return String.format(Locale.getDefault(), "%d,%d,%s,%s,%s", sEventTime,
                sSensorId, fmtVal(sValue_x), fmtVal(sValue_y), fmtVal(sValue_z));
    }

    private String fmtVal(float value) {
        return String.format("%014.10f", value);
    }
    private String fmtVal(float value_x, float value_y, float value_z) {
        return String.format("%014.10f,%014.10f,%014.10f", value_x, value_y, value_z);
    }


    /**
     * For machine learning algorithm, accelerometer and gyroscope values along
     * three dimensions are converted into a single value
     * squareRoot(x*x+y*y+z*z). Calculating square root is very costly operation
     * for sender (which is a Android device). Therefore it just sends
     * (x*x+y*y+z*z) and then square root is calculated by receiver.
     *
     * @param val
     * @return
     */
    private float sqrt(float val) {
        return (float) Math.sqrt(val);
    }

    @Override
    public String toString() {
        switch (getSensorType()) {
            case ACCELEROMETER:
                return formatAcc();
            case GYROSCOPE:
                return formatGyro();
            default:
                throw new IllegalArgumentException("Incorrect sensor type");
        }
    }
}