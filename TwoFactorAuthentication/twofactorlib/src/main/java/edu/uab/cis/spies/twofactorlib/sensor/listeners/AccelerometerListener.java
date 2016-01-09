
package edu.uab.cis.spies.twofactorlib.sensor.listeners;

import android.hardware.SensorEvent;

import edu.uab.cis.spies.twofactorlib.enumerations.SensorTypes;
import edu.uab.cis.spies.twofactorlib.queues.SensorValuesQs;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AccelerometerListener extends SensorListener {

    public AccelerometerListener(SensorValuesQs sensorValsQs) {
        super(SensorTypes.ACCELEROMETER, sensorValsQs);
    }

    @Override
    public float formatValue(SensorEvent event) {
        return (event.values[0] * event.values[0])
                + (event.values[1] * event.values[1])
                + (event.values[2] * event.values[2]);
    }
}