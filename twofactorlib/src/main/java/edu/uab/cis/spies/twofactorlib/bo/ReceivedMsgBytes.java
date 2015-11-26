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

package edu.uab.cis.spies.twofactorlib.bo;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class ReceivedMsgBytes {

    private final byte[] sBytes;
    // received number of bytes are always fixed. however in this array how many
    // bytes are really read.
    private final int sLen;

    public ReceivedMsgBytes(byte[] byts, int len) {
        super();
        this.sBytes = byts;
        this.sLen = len;
    }

    public byte[] getBytes() {
        return this.sBytes;
    }

    public int getLen() {
        return this.sLen;
    }
}
