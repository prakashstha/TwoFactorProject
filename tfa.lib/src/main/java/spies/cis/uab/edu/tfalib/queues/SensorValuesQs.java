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

package spies.cis.uab.edu.tfalib.queues;

import java.util.concurrent.LinkedBlockingQueue;

import spies.cis.uab.edu.tfalib.bo.SensorValue;


/**
 * Input source for all these Q's is same, however they are utilized by
 * different workers for separate and independent purpose.
 * 
 * @author Swapnil Udar
 * 
 */
public class SensorValuesQs {

	public SensorValuesQs() {
		super();
	}

	/**
	 * Irrespective of execution mode sensor values collected over the BT
	 * connections are segmented w.r.t. input events and then utilized for
	 * classification directly or can be stored them onto a file.
	 * 
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