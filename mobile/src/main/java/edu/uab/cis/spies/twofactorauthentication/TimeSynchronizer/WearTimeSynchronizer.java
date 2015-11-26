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

package edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;
import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationRequest;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationResponse;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeRequest;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeResponse;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class WearTimeSynchronizer extends TwoFactorThread implements IMessageHandler {

    private static final String LOG_TAG = WearTimeSynchronizer.class
            .getSimpleName();
    private static final long TIME_SYNC_AFTER_EVERY_MS = 1000;
    private static final long TIME_SYNC_DURATION_MS = 1500;

    private long sTimeSyncMsgCounter, sTimeSyncMsgCounter1;
    private long sAvgM2WTripTime;
    private long sAvgM2WTimeDiff, sAvgM2WTimeDiff1;
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ;
    private List<Integer> sReqMsgIds = new ArrayList<Integer>();
    private FileUtility fileUtility;
    private String wearTimeSyncFilePath;
    private BufferedWriter bfrWriter = null;

    public WearTimeSynchronizer(ThreadGroup tGroup,
                                LinkedBlockingQueue<IMessage> outboundMsgQ,
                                FileUtility fileUtility) {
        super(tGroup, LOG_TAG);
        this.fileUtility = fileUtility;
        this.sTimeSyncMsgCounter = 0;
        this.sTimeSyncMsgCounter1 = 0;
        this.sAvgM2WTripTime = 0;
        this.sAvgM2WTimeDiff = 0;
        this.sAvgM2WTimeDiff1 = 0;
        this.sOutboundMsgQ = outboundMsgQ;
    }

    public long getAvgM2WTimeDiff() {

        return sAvgM2WTimeDiff;
    }

    public long getsAvgM2WTripTime() {
        return sAvgM2WTripTime;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG, "Interrupted");
    }

    @Override
    protected void mainloop() {
        Log.d(LOG_TAG, ThreadStatus.RUNNING);
        /* Initializing buffer writer */
        try {
            wearTimeSyncFilePath = fileUtility.getWearTimeSyncFilePath();
            if (wearTimeSyncFilePath != null && wearTimeSyncFilePath.length() != 0) {
                bfrWriter = new BufferedWriter(new FileWriter(wearTimeSyncFilePath));
            } else
                throw new IllegalArgumentException("Could not create wear time sync file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        while (!isInterrupted()) {
            //Log.d(LOG_TAG, "Time synchronization started");

            // If loop terminated because of interruption
            if (isInterrupted()) {
                Log.d(LOG_TAG, ThreadStatus.INTERRUPTED);
                break;
            }
            // Send share time request.
            sendShareTimeReq();
            // perform time synchronization after TIME_SYNC_AFTER_EVERY_MS
            // milliseconds
            try {
                takeRest(TIME_SYNC_AFTER_EVERY_MS);
            } catch (InterruptedException e) {
                Log.d(LOG_TAG, ThreadStatus.INTERRUPTED + ": breaking loop");
                break;
            }


        }
        Log.d(LOG_TAG, ThreadStatus.END);

        /*closing bufferwriter*/
        if(bfrWriter!=null){
            try {
                bfrWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bfrWriter = null;
        }
    }

    private void sendRTTCalcReq() throws InterruptedException {
        RTTCalculationRequest rttReq = new RTTCalculationRequest();
        sReqMsgIds.add(rttReq.getMsgId());
        sOutboundMsgQ.add(rttReq);
    }

    private void sendShareTimeReq() {
        // Send share time request.
        ShareTimeRequest shareTimeReq = new ShareTimeRequest();
        sReqMsgIds.add(shareTimeReq.getMsgId());
        sOutboundMsgQ.add(shareTimeReq);
    }

    /**
     * Handles RTTCalculationResponse and ShareTimeResponse messages
     */
    @Override
    public void handle(IMessage msg) {
        // Check if the message id matches with one of the request's message id.
        if (!sReqMsgIds.remove((Integer) msg.getMsgId())) {
            throw new IllegalArgumentException("Incorrect msg");
        }

        switch (msg.getMsgType()) {
            case RTTCalculationResponse.MSG_TYPE:
                updateTimeSyncParams(msg);
                break;
            case ShareTimeResponse.MSG_TYPE:
                updateAvgTimeDiff(msg);
                break;
            default:
                throw new IllegalArgumentException("Incorrect msg");
        }
    }

    private void updateTimeSyncParams(IMessage msg) {
        if (!(msg instanceof RTTCalculationResponse)) {
            throw new IllegalArgumentException("Incorrect msg");
        }
        RTTCalculationResponse response = (RTTCalculationResponse) msg;
        long oneWayTimeDelay = (System.currentTimeMillis() - response
                .getRequesterTimestmp()) / 2;
        sAvgM2WTripTime = ((sAvgM2WTripTime * sTimeSyncMsgCounter) + oneWayTimeDelay)
                / (sTimeSyncMsgCounter + 1);
        sTimeSyncMsgCounter = sTimeSyncMsgCounter + 1;
    }

    private void updateAvgTimeDiff(IMessage msg) {
        if (!(msg instanceof ShareTimeResponse)) {
            throw new IllegalArgumentException("Incorrect msg");
        }
        ShareTimeResponse response = (ShareTimeResponse) msg;
        long responseTime = System.currentTimeMillis();
        long serverTime = response.getServerTime();
        long requestTime = response.getsRequesterTimestamp();
        long oneWayTimeDelay = (responseTime - requestTime) / 2;
        sAvgM2WTimeDiff = (((responseTime - oneWayTimeDelay) - serverTime) + sAvgM2WTimeDiff * sTimeSyncMsgCounter1) / (sTimeSyncMsgCounter1 + 1);
        //Log.d(LOG_TAG,"One way time delay: " + oneWayTimeDelay);
        Log.d(LOG_TAG, "[ Avg Time diff: " + sAvgM2WTimeDiff + " ms ]");
        sTimeSyncMsgCounter1 = sTimeSyncMsgCounter1 + 1;

        /* writing all data on file */
        String str = String.format(Locale.getDefault(), "%d,%d,%d,%d,%d", requestTime, responseTime, serverTime,oneWayTimeDelay,sAvgM2WTimeDiff);
        if(bfrWriter!=null) {
            try {
                bfrWriter.append(str + "\n");
                bfrWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}