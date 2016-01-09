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

package spies.cis.uab.edu.tfalib.service;

/**
 * 
 * @author Swapnil Udar
 *
 */
public enum ServiceMessage {
	INFO, BEGIN, BEGIN_ACK, ERROR, STOP, STOP_ACK, STOPPED;

	public static ServiceMessage getServiceMsg(int ordinal) {
		for (ServiceMessage msg : values()) {
			if (msg.ordinal() == ordinal) {
				return msg;
			}
		}
		throw new IllegalArgumentException("Incorrect service messege");
	}
}