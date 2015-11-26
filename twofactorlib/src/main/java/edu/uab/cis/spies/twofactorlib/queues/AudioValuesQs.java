package edu.uab.cis.spies.twofactorlib.queues;

import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.AudioValue;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioValuesQs {
    private final LinkedBlockingQueue<AudioValue> sAudioValQs = new LinkedBlockingQueue<AudioValue>();
    /**
     * Irrespective of the mode of execution this Q is required for collecting
     * statistics of the sensor values.
     */

    public void add(AudioValue value) {
        sAudioValQs.add(value);
    }

    public void add(byte[] value, int noOfShorts) {
        sAudioValQs.add(new AudioValue(value, noOfShorts));
    }


    public LinkedBlockingQueue<AudioValue> getAudioValueQs() {
        return sAudioValQs;
    }

    public void clear() {
        sAudioValQs.clear();
    }
}
