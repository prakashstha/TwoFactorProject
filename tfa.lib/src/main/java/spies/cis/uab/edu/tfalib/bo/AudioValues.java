package spies.cis.uab.edu.tfalib.bo;

import android.provider.MediaStore;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import spies.cis.uab.edu.tfalib.common.AudioParameters;
import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;

import static java.util.Arrays.copyOfRange;
import static spies.cis.uab.edu.tfalib.common.Constants.SHORT_SIZE;

/**
 * Created by prakashs on 7/31/2015.
 */
public class AudioValues {
    private final List<AudioValue> sAudioValues;

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
        this(audioVals.toArray(new AudioValue[audioVals.size()]));
    }
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
    //need to change
    public short[] asValues() {
        short[] values = new short[sAudioValues.size()];
        for (int i = 0; i < sAudioValues.size(); i++) {
            values[i] = sAudioValues.get(i).getValue();
        }
        return values;
    }
    public void clear() {
        sAudioValues.clear();
    }

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
    private boolean isNumOfBytsValid(byte[] rawByts) {
        if (rawByts == null || rawByts.length == 0) {
            return false;
        }
        int len = rawByts.length;
        if (len > 0 && len < (AudioValue.LEN + SHORT_SIZE)) {
            return false;
        }
        int size = ByteBuffer.wrap(Arrays.copyOfRange(rawByts, 0, 2))
                .order(ByteOrder.BIG_ENDIAN).getShort();
        if ((size < 0) || (len < ((size * AudioValue.LEN) + 2))) {
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
        for (AudioValue audioValue : sAudioValues) {
            toString.append("\n").append(audioValue.toString());
        }
        return toString.toString();
    }

}
