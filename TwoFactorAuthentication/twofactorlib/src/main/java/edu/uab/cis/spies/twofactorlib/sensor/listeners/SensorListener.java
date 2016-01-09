
package edu.uab.cis.spies.twofactorlib.sensor.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import edu.uab.cis.spies.twofactorlib.bo.SensorValue;
import edu.uab.cis.spies.twofactorlib.enumerations.SensorTypes;
import edu.uab.cis.spies.twofactorlib.queues.SensorValuesQs;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public abstract class SensorListener implements SensorEventListener {
    private static final int NANOSECOND_TO_MILLISECOND = 1000000;


    private final SensorTypes sSensorType;
    private final SensorValuesQs sSensorValsQs;
    private final String sLogTag;
    private long avgDiff = 0;
    private long numberOfMeasurements = 0;


    public SensorListener(SensorTypes sensorType, SensorValuesQs sensorValsQs) {
        super();
        sSensorType = sensorType;
        sSensorValsQs = sensorValsQs;
        sLogTag = sensorType.name() + "_Listener";

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        switch (accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                Log.d(sLogTag, sSensorType.name() + " maximum accuracy");
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                throw new IllegalArgumentException(sSensorType.name()
                        + " low accuracy");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        event.timestamp = event.timestamp / NANOSECOND_TO_MILLISECOND;
        avgDiff = ((numberOfMeasurements++) * avgDiff + (System
                .currentTimeMillis() - event.timestamp)) / numberOfMeasurements;

        event.timestamp = event.timestamp + avgDiff;
        //Log.d(sLogTag, "reading...: "+sSensorType+ String.valueOf(formatValue(event)));
        //sSensorValsQs.add(new SensorValue(formatValue(event), sSensorType.getSensorId(), event.timestamp));
        sSensorValsQs.add(new SensorValue(event.values, sSensorType.getSensorId(), event.timestamp));
        // Log.d(sLogTag,"Size Acc" + sSensorValsQs.getAccSegmentersQ().size()+"\n Gyro Size" + sSensorValsQs.getGyroSegmentersQ().size());
    }


    abstract protected float formatValue(SensorEvent event);
}