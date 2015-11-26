
package edu.uab.cis.spies.twofactorlib.sensor;


import edu.uab.cis.spies.twofactorlib.common.Constants;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public enum SensorTypes {
    ACCELEROMETER(Constants.ACCELROMETER), GYROSCOPE(Constants.GYROSCOPE);

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
        }
        throw new RuntimeException("Incorrect sensor type");
    }

    public byte getSensorId() {
        return this.sSensorId;
    }
}