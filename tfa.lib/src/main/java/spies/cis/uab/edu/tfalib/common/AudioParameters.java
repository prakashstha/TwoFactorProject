package spies.cis.uab.edu.tfalib.common;

import android.media.AudioFormat;
import android.media.AudioRecord;

/**
 * Created by Prakashs on 8/2/15.
 */
public interface AudioParameters {
    public static final int RECORDER_BPP = 16;
    public static final int RECORDER_SAMPLERATE = 44100;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int channels = 1;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final short bufferSize = (short)AudioRecord.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            RECORDER_AUDIO_ENCODING);

}
