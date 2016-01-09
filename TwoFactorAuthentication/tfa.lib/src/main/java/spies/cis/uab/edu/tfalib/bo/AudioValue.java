package spies.cis.uab.edu.tfalib.bo;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;

import spies.cis.uab.edu.tfalib.common.AudioParameters;
import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;

/**
 * Created by prakashs on 7/31/2015.
 */
public class AudioValue {

    public static final String LOG_TAG = AudioValue.class.getSimpleName();
    public static final short LEN = (short)(Constants.SHORT_SIZE );
    protected short sValue;

    public AudioValue(short value) {
        this.sValue = value;
     }

    public AudioValue(byte[] byts){
        if(byts.length != LEN)
        {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        int start = 0;
        int end = Constants.SHORT_SIZE;
        sValue = ConversionUtil.conv2Short(Arrays.copyOfRange(byts, start, end));
    }
    public short getValue(){
        return sValue;
    }
    public int getLen(){
        return this.LEN;
    }

    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(LEN);
        bytBuffer.putShort(sValue);
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }
    public static byte[] toByteArray(AudioValue[] msgs) {
        ByteBuffer bBuffer = ByteBuffer.allocate(calcLenOfMsgs(msgs));
        for (AudioValue msg : msgs) {
            bBuffer.put(msg.toByteArray());
        }
        return bBuffer.array();
    }
    public static int calcLenOfMsgs(AudioValue[] msgs) {
        int len = 0;
        for (AudioValue msg : msgs) {
            len = len + msg.getLen();
        }
        return len;
    }

    public String formatValue() {
        return String.valueOf(sValue);

    }
    @Override
    public String toString() {
       return formatValue();
    }

}
