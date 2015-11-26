/*
 * Copyright 2015 (c) Secure System Group (https://se-sy.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spies.cis.uab.edu.tfalib.sensor;


import spies.cis.uab.edu.tfalib.common.Constants;

/**
 *
 * @author Swapnil Udar
 *
 */
public enum SensorTypes {
    ACCELEROMETER(Constants.ACCELROMETER), GYROSCOPE(Constants.GYROSCOPE);

    private final byte sSensorId;

    private SensorTypes(byte sensorId) {
        this.sSensorId = sensorId;
    }

    public byte getSensorId() {
        return this.sSensorId;
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
}