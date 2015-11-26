package edu.uab.cis.spies.twofactorlib.bo;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.AudioParameters;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * <p>
 *     Audio data buffer provides byte array. This class holds the
 *     byte array of audio data fetched at each time.
 *     Size of byte array is equal to <code>bufferSize</code>
 * <p/>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioValue {

    public static final String LOG_TAG = AudioValue.class.getSimpleName();
    public static final short bufferSize = AudioParameters.bufferSize;
    public static final short LEN = (short)(1*bufferSize);//BYTE size = 1
    protected byte []sValue = new byte[bufferSize];


    public AudioValue(byte[] value, int noOfBytes) {

        if(noOfBytes<=bufferSize){
            System.arraycopy(value, 0, sValue, 0, noOfBytes);
            Arrays.fill(sValue, noOfBytes, bufferSize, (byte) 0);
        }
        else if(sValue.length>bufferSize)
            Log.e(LOG_TAG, " Audio value size greater than buffer size: " + value.length);
    }

    public AudioValue(byte[] byts){
        if(byts.length != LEN)
        {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        System.arraycopy(byts, 0, sValue, 0, byts.length);
    }
    public byte[] getValue(){
        return sValue;
    }

    public int getLen(){
        return this.LEN;
    }

    /**
     *
     * @return byte[] byte array representation of AudioValue
     */
    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(LEN);
        bytBuffer.put(sValue);
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

    /**
     *
     * @return String representation of AudioValue
     */
    public String formatValue() {
        String str = "";
        StringBuilder sb = new StringBuilder();
        int i;
        for(i = 0;i<(sValue.length-1);i++){
            str =String.valueOf(sValue[i])+",";
            sb.append(str);
        }
        str = String.valueOf(sValue[i]);
        sb.append(str);
        return sb.toString();
    }
    @Override
    public String toString() {
        return formatValue();
    }

}
