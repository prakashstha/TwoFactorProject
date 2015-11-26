package edu.uab.cis.spies.twofactorlib.enumerations;

import edu.uab.cis.spies.twofactorlib.common.Constants;

/**
 * <p>
 *     enum to uniquely identify the sensor type.
 *     Sensor could be:
 *     <ul>
 *         <li>ACCELEROMETER</li>
 *         <li>GYROSCOPE</li>
 *         <li>AUDIO</li>
 *     </ul>
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
*/
public enum SensorTypes {
    ACCELEROMETER(Constants.ACCELROMETER), GYROSCOPE(Constants.GYROSCOPE), AUDIO(Constants.AUDIO);

    private final byte sSensorId;

    private SensorTypes(byte sensorId) {
        this.sSensorId = sensorId;
    }

    public static SensorTypes getSensorType(byte sensorId) {
        switch (sensorId) {
            case Constants.ACCELROMETER:
                return SensorTypes.ACCELEROMETER;
            case Constants.GYROSCOPE:
                return SensorTypes.GYROSCOPE;
            case Constants.AUDIO:
                return SensorTypes.AUDIO;
        }
        throw new RuntimeException("Incorrect sensor type");
    }
    public byte getSensorId() {
        return this.sSensorId;
    }
}