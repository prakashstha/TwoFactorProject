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

package spies.cis.uab.edu.tfalib.bo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;


import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;
import spies.cis.uab.edu.tfalib.enumerations.SensorTypes;

/**
 * 
 * @author Swapnil Udar
 * 
 */
public class SensorValue {
	public static final short LEN = Constants.FLOAT_SZ + Constants.BYTE_SIZE
			+ Constants.LONG_SIZE;

	protected final float sValue;
	protected final byte sSensorId;
	protected long sEventTime;

	public SensorValue(float value, byte sensorId, long eventTsmp) {
		super();
		this.sValue = value;
		this.sSensorId = sensorId;
		this.sEventTime = eventTsmp;
	}

	// Sensor values sent from an Android device are supposed to be root mean
	// square of sensor values observed along three axis. However, calculating
	// square root is quite expensive operation on the device. So, it has been
	// deferred to the receiving device. Receiving device will receive a
	// sequence of byte stream from it, sensor value instance will be
	// constructed. Square root of value is taken.  
	public SensorValue(byte[] byts) {

		if (byts.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}

		int start = 0;
		int end = start + Constants.FLOAT_SZ;
		sValue = sqrt(ConversionUtil.conv2Float(Arrays.copyOfRange(byts, start,
                end)));

		start = end;
		end = start + Constants.LONG_SIZE;
		sEventTime = ConversionUtil.conv2Long(Arrays.copyOfRange(byts, start,
				end));

		sSensorId = byts[end];
	}
	
	// Used when creating instance for testing purpose
	public SensorValue(String sensorValue) {
		super();

		if (sensorValue == null || sensorValue.trim().length() == 0) {
			throw new IllegalArgumentException("Incorrect message string");
		}

		String[] split = sensorValue.split(",");
		if (split.length != 3) {
			throw new IllegalArgumentException("Incorrect message string");
		}
		this.sEventTime = Long.valueOf(split[0]);
		this.sSensorId = Byte.valueOf(split[1]);
		this.sValue = Float.valueOf(split[2]);
	}

	public SensorTypes getSensorType() {
		return SensorTypes.getSensorType(sSensorId);
	}

	public byte getSensorId() {
		return sSensorId;
	}

	public float getValue() {
		return sValue;
	}

	public long getTime() {
		return sEventTime;
	}
	
	public byte[] toByteArray() {
		ByteBuffer bytBuffer = ByteBuffer.allocate(LEN);
		bytBuffer.putFloat(sValue);
		bytBuffer.putLong(sEventTime);
		bytBuffer.put(sSensorId);
		bytBuffer.order(ByteOrder.BIG_ENDIAN);
		return bytBuffer.array();
	}

	/*public void syncTime(long syncTime) {
		sEventTime = sEventTime + syncTime;
	}*/

	public String formatAcc() {
		return String.format(Locale.getDefault(), "%d,%d,%s", sEventTime,
				sSensorId, fmtVal(sValue));
	}

	public String formatGyro() {
		return String.format(Locale.getDefault(), "%d,%d,%s", sEventTime,
				sSensorId, fmtVal(sValue));
	}

	private String fmtVal(float value) {
		return String.format("%014.10f", value);
	}

	/**
	 * For machine learning algorithm, accelerometer and gyroscope values along
	 * three dimensions are converted into a single value
	 * squareRoot(x*x+y*y+z*z). Calculating square root is very costly operation
	 * for sender (which is a Android device). Therefore it just sends
	 * (x*x+y*y+z*z) and then square root is calculated by receiver.
	 * 
	 * @param val
	 * @return
	 */
	private float sqrt(float val) {
		return (float) Math.sqrt(val);
	}

	@Override
	public String toString() {
		switch (getSensorType()) {
		case ACCELEROMETER:
			return formatAcc();
		case GYROSCOPE:
			return formatGyro();
		default:
			throw new IllegalArgumentException("Incorrect sensor type");
		}
	}
}