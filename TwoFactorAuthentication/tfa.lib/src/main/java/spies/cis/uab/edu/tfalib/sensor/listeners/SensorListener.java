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

package spies.cis.uab.edu.tfalib.sensor.listeners;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import spies.cis.uab.edu.tfalib.bo.SensorValue;
import spies.cis.uab.edu.tfalib.enumerations.SensorTypes;
import spies.cis.uab.edu.tfalib.queues.SensorValuesQs;

/**
 * 
 * @author Swapnil Udar
 *
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
        sSensorValsQs.add(new SensorValue(formatValue(event),sSensorType.getSensorId(),event.timestamp));
       // Log.d(sLogTag,"Size Acc" + sSensorValsQs.getAccSegmentersQ().size()+"\n Gyro Size" + sSensorValsQs.getGyroSegmentersQ().size());
	}




	abstract protected float formatValue(SensorEvent event);
}