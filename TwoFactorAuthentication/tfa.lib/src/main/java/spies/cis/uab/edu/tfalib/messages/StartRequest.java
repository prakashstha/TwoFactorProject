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


/**
 * 
 * @author Swapnil Udar
 *
 */
public class StartRequest extends Message {
	public static final short LEN = Header.LEN;
	public static final byte MSG_TYPE = 0x20;

	public StartRequest(byte[] msg) {
		super(msg);
		if (msg.length != LEN) {
			throw new IllegalArgumentException("Incorrect message bytes");
		}
	}

	public StartRequest() {
		super(MSG_TYPE, LEN, generateMsgId());
	}

	@Override
	public void validateMsgType() {
		if (getMsgType() != MSG_TYPE) {
			throw new IllegalArgumentException("Incorrect msg type");
		}
	}
}