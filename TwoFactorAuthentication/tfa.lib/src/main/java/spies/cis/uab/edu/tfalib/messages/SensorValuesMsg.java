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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import spies.cis.uab.edu.tfalib.bo.SensorValue;
import spies.cis.uab.edu.tfalib.bo.SensorValues;
import spies.cis.uab.edu.tfalib.messages.Message;


/**
 *
 * @author Swapnil Udar
 *
 */
public class SensorValuesMsg extends Message {
    public static final byte MSG_TYPE = 0x40;

    private final SensorValues sSensorValues;

    public SensorValuesMsg(byte[] msg) {
        super(msg);
        // Check minimum length of sensor value message
        if (msg == null || msg.length < (Header.LEN + SensorValue.LEN)) {
            throw new IllegalArgumentException("Incorrect message bytes");
        }
        this.sSensorValues = new SensorValues(Arrays.copyOfRange(msg,
                Header.LEN, msg.length));
    }

    public SensorValuesMsg(SensorValues sensorVals) {
        super(MSG_TYPE, calcMsgLen(sensorVals), generateMsgId());
        this.sSensorValues = sensorVals;
    }

    private static short calcMsgLen(SensorValues sensorVals) {
        return (short) (Header.LEN + sensorVals.getBytesLen());
    }

    @Override
    public void validateMsgType() {
        if (getMsgType() != MSG_TYPE) {
            throw new IllegalArgumentException("Incorrect msg type");
        }
    }

    public SensorValues getSensorValues() {
        return sSensorValues;
    }

    public byte[] toByteArray() {
        ByteBuffer bytBuffer = ByteBuffer.allocate(Header.LEN
                + sSensorValues.getBytesLen());
        bytBuffer.clear();
        bytBuffer.put(super.toByteArray());
        bytBuffer.put(sSensorValues.toByteArray());
        bytBuffer.order(ByteOrder.BIG_ENDIAN);
        return bytBuffer.array();
    }

    @Override
    public String toString() {
        return sSensorValues.toString();
    }
}