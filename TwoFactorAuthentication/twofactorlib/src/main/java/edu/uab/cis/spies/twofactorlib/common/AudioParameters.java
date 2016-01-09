package edu.uab.cis.spies.twofactorlib.common;

import android.media.AudioFormat;
import android.media.AudioRecord;

/**
 * <p>
 *     Interface to hold audio parameters <code>RECORDER_BPP, RECORDER_SAMPLERATE,
 *     RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize</code>
 * <p/>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface AudioParameters {
    public static final short RECORDER_BPP = 16;
    public static final int RECORDER_SAMPLERATE = 22050;//44100;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final short channels = 1;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final short bufferSize = 1024;

}
