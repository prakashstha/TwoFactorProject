package edu.uab.cis.spies.twofactorlib.queues;

import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.SensorValue;


/**
 * Input source for all these Q's is same, however they are utilized by
 * different workers for separate and independent purpose.
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValuesQs {

    /**
     * Irrespective of execution mode sensor values collected over the BT
     * connections are segmented w.r.t. input events and then utilized for
     * classification directly or can be stored them onto a file.
     * <p/>
     * Unlike sensor values file writer and statisticians Q, there are sensor
     * specific sensor value Q for segmenter, because segmenter processes values
     * separately. As per the current state, this program supports three
     * sensors, therefore thre Q's.
     */
    private final LinkedBlockingQueue<SensorValue> sAccSegmentersQ = new LinkedBlockingQueue<SensorValue>();
    private final LinkedBlockingQueue<SensorValue> sGyroSegmentersQ = new LinkedBlockingQueue<SensorValue>();
    /**
     * Irrespective of the mode of execution this Q is required for collecting
     * statistics of the sensor values.
     */
    private final LinkedBlockingQueue<SensorValue> sStatisticiansQ = new LinkedBlockingQueue<SensorValue>();

    public SensorValuesQs() {
        super();
    }

    public void add(SensorValue value) {
        switch (value.getSensorType()) {
            case ACCELEROMETER:
                sAccSegmentersQ.add(value);
                break;
            case GYROSCOPE:
                sGyroSegmentersQ.add(value);
                break;
        }
        sStatisticiansQ.add(value);
    }

    public LinkedBlockingQueue<SensorValue> getAccSegmentersQ() {
        return sAccSegmentersQ;
    }

    public LinkedBlockingQueue<SensorValue> getGyroSegmentersQ() {
        return sGyroSegmentersQ;
    }

    public LinkedBlockingQueue<SensorValue> getStatisticiansQ() {
        return sStatisticiansQ;
    }

    public void clear() {
        sAccSegmentersQ.clear();
        sGyroSegmentersQ.clear();
        sStatisticiansQ.clear();
    }
}