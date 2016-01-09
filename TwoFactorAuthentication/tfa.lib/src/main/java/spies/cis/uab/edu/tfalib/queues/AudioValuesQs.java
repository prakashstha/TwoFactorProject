package spies.cis.uab.edu.tfalib.queues;

import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.AudioValue;

/**
 * Created by prakashs on 7/31/2015.
 */
public class AudioValuesQs {
    private final LinkedBlockingQueue<AudioValue> sAudioValQs = new LinkedBlockingQueue<AudioValue>();
    /**
     * Irrespective of the mode of execution this Q is required for collecting
     * statistics of the sensor values.
     */
    private final LinkedBlockingQueue<AudioValue> sStatisticiansQ = new LinkedBlockingQueue<AudioValue>();

    public void add(AudioValue value) {
        sAudioValQs.add(value);
        sStatisticiansQ.add(value);
    }
    public void add(short[] value, int noOfShorts) {
        for(int i = 0;i<noOfShorts;i++)
        {
            sAudioValQs.add(new AudioValue(value[i]));
            sStatisticiansQ.add(new AudioValue(value[i]));
        }
    }


    public LinkedBlockingQueue<AudioValue> getAudioValueQs() {
        return sAudioValQs;
    }

    public LinkedBlockingQueue<AudioValue> getStatisticiansQ() {
        return sStatisticiansQ;
    }

    public void clear() {
        sAudioValQs.clear();
        sStatisticiansQ.clear();
    }
}
