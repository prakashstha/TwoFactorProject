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

package spies.cis.uab.edu.tfalib.messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import spies.cis.uab.edu.tfalib.common.ConversionUtil;

/**
 * 
 * @author Swapnil Udar
 *
 */
public final class MessageFactory {

	public static final List<IMessage> getMessages(byte[] byts) {
		List<IMessage> msgs = new ArrayList<IMessage>();
		int msgLen = 0;
		int index = 0;
		while (index < byts.length) {
			msgLen = ConversionUtil.conv2Short(Arrays.copyOfRange(byts,
                    index + 1, index + 3));
			if (msgLen <= 0 || (index + msgLen) > byts.length) {				
				// When bytes are not enough to create a message, then break the
				// loop. Then next received bytes will be appended to these
				// remaining bytes to create a legitimate message.
				break;
			}
			try {
				msgs.add(getMessage(Arrays.copyOfRange(byts, index, index
						+ msgLen)));
			} catch (IllegalArgumentException ile) {
				System.out.println("\nComplete message : "
						+ formatHexBytes(byts));
				System.out.println("\nFailed message : "
						+ formatHexBytes(Arrays.copyOfRange(byts, index, index
								+ msgLen)));
				throw ile;
			}
			index = index + msgLen;
		}
		return msgs;
	}

	public static String formatHexBytes(byte[] bytes) {
		StringBuilder result = new StringBuilder();
		for (byte byteData : bytes) {
			result.append(String.format("%02X ", byteData));
		}
		return result.toString().intern();
	}

	private static final IMessage getMessage(byte[] byts) {
		switch (byts[0]) {
		case SensorValuesMsg.MSG_TYPE:
			return new SensorValuesMsg(byts);
        case AudioValuesMsg.MSG_TYPE:
                return new AudioValuesMsg(byts);
//		case RTTCalculationRequest.MSG_TYPE:
//			return new RTTCalculationRequest(byts);
//		case RTTCalculationResponse.MSG_TYPE:
//			return new RTTCalculationResponse(byts);
//		case HeartBeatRequest.MSG_TYPE:
//			return new HeartBeatRequest(byts);
//		case HeartBeatResponse.MSG_TYPE:
//			return new HeartBeatResponse(byts);
		case StartRequest.MSG_TYPE:
			return new StartRequest(byts);
		case StartResponse.MSG_TYPE:
			return new StartResponse(byts);
		case StopRequest.MSG_TYPE:
			return new StopRequest(byts);
		case StopResponse.MSG_TYPE:
			return new StopResponse(byts);
//		case ShareTimeRequest.MSG_TYPE:
//			return new ShareTimeRequest(byts);
//		case ShareTimeResponse.MSG_TYPE:
//			return new ShareTimeResponse(byts);
//		case SendSensorValuesRequest.MSG_TYPE:
//			return new SendSensorValuesRequest(byts);
//		case StopSensorValuesRequest.MSG_TYPE:
//			return new StopSensorValuesRequest(byts);
//		case ProximityMsg.MSG_TYPE:
//			return new ProximityMsg(byts);
//		case ManageSensorValFreqMsg.MSG_TYPE:
//			return new ManageSensorValFreqMsg(byts);
//		case WarningMsg.MSG_TYPE:
//			return new WarningMsg(byts);
//		case SegmentFeaturesMsg.MSG_TYPE:
//			return new SegmentFeaturesMsg(byts);
//		case SensorValueStatisticsMsg.MSG_TYPE:
//			return new SensorValueStatisticsMsg(byts);
//		case EventDetailMsg.MSG_TYPE:
//			return new EventDetailMsg(byts);
//		case ClassificationResultMsg.MSG_TYPE:
//			return new ClassificationResultMsg(byts);
//		case FailedSegmentMsg.MSG_TYPE:
//			return new FailedSegmentMsg(byts);
//		case ConfigurationMsg.MSG_TYPE:
//			return new ConfigurationMsg(byts);
//		case SegmentMsg.MSG_TYPE:
//			return new SegmentMsg(byts);
//		case ShortHeartBeatResponse.MSG_TYPE:
//			return new ShortHeartBeatResponse(byts);
		default:
			throw new IllegalArgumentException(
					"Byte sequence does not match with message");
		}
	}
}