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

import static spies.cis.uab.edu.tfalib.common.Constants.SHORT_SIZE;
import static java.util.Arrays.copyOfRange;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;
import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.common.ConversionUtil;

/**
 * 
 * @author Swapnil Udar
 *
 */
public class SensorValues {
	private final List<SensorValue> sSensorValues;

	public SensorValues(byte[] msg) {
		super();
		this.sSensorValues = convert(msg);
	}

	public SensorValues(SensorValue[] sensorVals) {
		super();
		this.sSensorValues = new ArrayList<SensorValue>(
				Arrays.asList(sensorVals));
	}

	public SensorValues() {
		super();
		this.sSensorValues = new ArrayList<SensorValue>();
	}

	public SensorValues(List<SensorValue> sensorVals) {
		this(sensorVals.toArray(new SensorValue[sensorVals.size()]));
	}

	public int getBytesLen() {
		// number of sensor values + sensor values
		return (short) (Constants.SHORT_SIZE + (SensorValue.LEN * sSensorValues
				.size()));
	}

	public void add(SensorValue sensorVal) {
		synchronized (this) {
			sSensorValues.add(sensorVal);
		}
	}

	public List<SensorValue> asList() {
		return sSensorValues;
	}

	public SensorValue[] asArray() {
		return sSensorValues.toArray(new SensorValue[sSensorValues.size()]);
	}

	public float[] asValues() {
		float[] values = new float[sSensorValues.size()];
		for (int i = 0; i < sSensorValues.size(); i++) {
			values[i] = sSensorValues.get(i).getValue();
		}
		return values;
	}

	public void clear() {
		sSensorValues.clear();
	}

	public byte[] toByteArray() {
		ByteBuffer bytBuffer = ByteBuffer.allocate(getBytesLen());
		bytBuffer.clear();
		bytBuffer.putShort((short) sSensorValues.size());
		for (SensorValue sensorVal : sSensorValues) {
			bytBuffer.put(sensorVal.toByteArray());
		}
		bytBuffer.order(ByteOrder.BIG_ENDIAN);
		return bytBuffer.array();
	}

	private List<SensorValue> convert(byte[] rawByts) {
		if (!isNumOfBytsValid(rawByts)) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
		short size = ConversionUtil.conv2Short(copyOfRange(rawByts, 0, 2));
		List<SensorValue> sensorVals = new ArrayList<SensorValue>(size);
		int startIndex = 0;
		int endIndex = 2;
		for (int i = 0; i < size; i++) {
			startIndex = endIndex;
			endIndex = startIndex + SensorValue.LEN;
			sensorVals.add(new SensorValue(Arrays.copyOfRange(rawByts,
					startIndex, endIndex)));
		}
		return sensorVals;
	}

	private boolean isNumOfBytsValid(byte[] rawByts) {
		if (rawByts == null || rawByts.length == 0) {
			return false;
		}
		int len = rawByts.length;
		if (len > 0 && len < (SensorValue.LEN + SHORT_SIZE)) {
			return false;
		}
		int size = ByteBuffer.wrap(Arrays.copyOfRange(rawByts, 0, 2))
				.order(ByteOrder.BIG_ENDIAN).getShort();
		if ((size < 0) || (len < ((size * SensorValue.LEN) + 2))) {
			return false;
		}
		return true;
	}

	public String format() {
		return toString();
	}

	@Override
	public String toString() {
		StringBuffer toString = new StringBuffer();
		for (SensorValue sensorVal : sSensorValues) {
			toString.append("\n").append(sensorVal.toString());
		}
		return toString.toString();
	}
}