package edu.uab.cis.spies.twofactorlib.bo;

import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

import static java.util.Arrays.copyOfRange;

/**
 * <p>
 *     list of <code>AudioValue</code>
 * </p>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioValues {
    private final List<AudioValue> sAudioValues;
    private final String LOG_TAG = AudioValues.class.getSimpleName();

    public AudioValues(byte[] msg) {
        super();
        this.sAudioValues = convert(msg);
    }
    public AudioValues(AudioValue[] audioVals) {
        super();
        this.sAudioValues = new ArrayList<AudioValue>(
                Arrays.asList(audioVals));
    }
    public AudioValues() {
        super();
        this.sAudioValues = new ArrayList<AudioValue>();
    }

    public AudioValues(List<AudioValue> audioVals) {
        this.sAudioValues = audioVals;
    }

    /**
     *
     * @return
     *        <code>LEN<code/> of data.
     *        : number of audio values + audio values
     */
    public int getBytesLen() {
        // number of audio values + audio values
        return (short) (Constants.SHORT_SIZE + (AudioValue.LEN * sAudioValues.size()));
    }
    public void add(AudioValue audioVal) {
        synchronized (this) {
            sAudioValues.add(audioVal);
        }
    }

    public List<AudioValue> asList() {
        return sAudioValues;
    }

    public AudioValue[] asArray() {
        return sAudioValues.toArray(new AudioValue[sAudioValues.size()]);
    }

    public void clear() {
        sAudioValues.clear();
    }

    /**
     * converts <code>List<AudioValue><code/> to byte stream
     * @return
     *        byte array form of list of <code>AudioValue<code/>
     */
    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(getBytesLen());
        bytBuffer.clear();
        bytBuffer.putShort((short) sAudioValues.size());
        for (AudioValue audioValue : sAudioValues) {
            bytBuffer.put(audioValue.toByteArray());
        }
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    /**
     * converts raw bytes representation to <code>List<AudioValue></></code>
     * @param rawByts
     *          raw byte array
     * @return
     *          List of AudioValue from raw bytes array
     */
    private List<AudioValue> convert(byte[] rawByts) {
        if (!isNumOfBytsValid(rawByts)) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        short size = ConversionUtil.conv2Short(copyOfRange(rawByts, 0, 2));
        List<AudioValue> audioVals = new ArrayList<AudioValue>(size);
        int startIndex = 0;
        int endIndex = 2;
        for (int i = 0; i < size; i++) {
            startIndex = endIndex;
            endIndex = startIndex + AudioValue.LEN;
            audioVals.add(new AudioValue(Arrays.copyOfRange(rawByts,
                    startIndex, endIndex)));
        }
        return audioVals;
    }

    /**
     * check if raw bytes array has valid number of bytes or not
     * @param rawByts
     *          raw byte array
     * @return
     *          <code>True<code/> if number of raw bytes are valid
     *          <code>False<code/> if number of raw bytes are invalid
     */
    private boolean isNumOfBytsValid(byte[] rawByts) {
        if (rawByts == null || rawByts.length == 0) {
            return false;
        }
        int len = rawByts.length;
        if (len > 0 && len < (AudioValue.LEN + Constants.SHORT_SIZE)) {
            return false;
        }
        int size = ByteBuffer.wrap(Arrays.copyOfRange(rawByts, 0, 2))
                .order(ByteOrder.BIG_ENDIAN).getShort();
        if ((size < 0) || (len < ((size * AudioValue.LEN) + 2))) {
            return false;
        }
        return true;
    }

    /**
     *
     * @return String representation of <code>List<AudioValue></></><code/>
     */
    public String format() {
        return toString();
    }
    @Override
    public String toString() {
        StringBuffer toString = new StringBuffer();
        // Log.d(LOG_TAG, "list  of audio size: " + sAudioValues.size());
        for (AudioValue audioValue : sAudioValues) {
            toString.append("\n").append(audioValue.toString());
        }
        return toString.toString();
    }

}
