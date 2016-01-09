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

package edu.uab.cis.spies.twofactorlib.messages;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;


/**
 * <p>
 *     Creates a <code>Header</code> for a message.
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public abstract class Message implements IMessage {

    private final Header sHeader;

    public Message(byte[] msg) {
        super();
        this.sHeader = new Header(Arrays.copyOfRange(msg, 0, Header.LEN));
        validateMsgType();
    }

    public Message(byte type, short len, int id) {
        super();
        this.sHeader = new Header(type, len, id);
    }

    protected static int generateMsgId() {
        return new Random().nextInt();
    }

    public static byte[] toByteArray(IMessage[] msgs) {
        ByteBuffer bBuffer = ByteBuffer.allocate(calcLenOfMsgs(msgs));
        for (IMessage msg : msgs) {
            bBuffer.put(msg.toByteArray());
        }
        return bBuffer.array();
    }

    public static int calcLenOfMsgs(IMessage[] msgs) {
        int len = 0;
        for (IMessage msg : msgs) {
            len = len + msg.getMsgLen();
        }
        return len;
    }

    public byte[] toByteArray() {
        return sHeader.toByteArray();
    }

    public void handle(IMessageHandler msgHandler) {
        msgHandler.handle(this);
    }

    public byte getMsgType() {
        return sHeader.getMsgType();
    }

    public short getMsgLen() {
        return sHeader.getMsgLen();
    }

    public int getMsgId() {
        return sHeader.getMsgId();
    }
}