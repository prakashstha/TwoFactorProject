package edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;
import edu.uab.cis.spies.twofactorlib.common.Constants;

/**
 * Created by Prakashs on 8/15/15.
 *
 * This class does the time synchronization similar to WearTImeSynchronizer but using MessageListener
 *
 */
public class WearTimeSynchronizer1 extends TwoFactorThread implements MessageApi.MessageListener{


    private static final String LOG_TAG = WearTimeSynchronizer1.class.getSimpleName();
    private MessageSender msgSender = null;
    private long sTimeSyncMsgCounter;
    private long sAvgM2WTripTime;
    private long sAvgM2WTimeDiff;

   // private GoogleApiClient mApiClient = null;


    public WearTimeSynchronizer1(ThreadGroup tGroup, GoogleApiClient client){
        super(tGroup, LOG_TAG);
        this.sTimeSyncMsgCounter = 0;
        this.sAvgM2WTripTime = 0;
        this.sAvgM2WTimeDiff = 0;
        msgSender = new MessageSender(client, this);
        Log.d(LOG_TAG,"WearTimeSynchronier1() constructor");
    }



    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase( Constants.START_ACK ) ) {
            byte[] msg = messageEvent.getData();
            String recvdMsg = new String(msg);
            String str[] = recvdMsg.split(",");
            long respTime = System.currentTimeMillis();
            long reqTime = Long.parseLong(str[0]);
            long serverTime = Long.parseLong(str[1]);

            long oneWayTimeDelay = (respTime - reqTime) / 2;
            sAvgM2WTripTime = ((sAvgM2WTripTime * sTimeSyncMsgCounter) + oneWayTimeDelay)
                    / (sTimeSyncMsgCounter + 1);
            sTimeSyncMsgCounter = sTimeSyncMsgCounter + 1;

            sAvgM2WTimeDiff = (respTime - sAvgM2WTripTime)
                    - serverTime;

            StringBuilder sb = new StringBuilder();
            sb.append(String.valueOf("[ RTT: " +  sAvgM2WTripTime ));
            sb.append(String.valueOf("ms  \t Offset: " + sAvgM2WTimeDiff+"ms]"));

            //mTxtViewInfo.setText(sb.toString());
            Log.d(LOG_TAG, sb.toString());

        }
    }

    @Override
    public void interrupt() {
        super.interrupt();
        if(msgSender!=null)
            msgSender.terminateConnection();
    }
    private static final long TIME_SYNC_AFTER_EVERY_MS = 5000;
    private static final long TIME_SYNC_DURATION_MS = 1500;

    @Override
    protected void mainloop() throws InterruptedException {
        Log.d(LOG_TAG,"Thread started synchronizing");
        while(!isInterrupted()){
            long tillTime = System.currentTimeMillis() + TIME_SYNC_DURATION_MS;
            long reqTime = System.currentTimeMillis();
            String reqTimeStr = String.valueOf(reqTime);

            while (!isInterrupted() && (System.currentTimeMillis() < tillTime)) {
                msgSender.sendMessage(Constants.APP_START, reqTimeStr.getBytes());
                takeRest(300);
            }
            takeRest(TIME_SYNC_AFTER_EVERY_MS);
//
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//
//            }
            if(isInterrupted()){
                break;
            }

        }
    }

  public class MessageSender
        {
        private long CONNECTION_TIME_OUT_MS = 1000;
        private GoogleApiClient mApiClient;
        private WearTimeSynchronizer1 msgHandler = null;
        private final String LOG_TAG = MessageSender.class.getSimpleName();

        public MessageSender(GoogleApiClient mApiClient, WearTimeSynchronizer1 msgHandler) {
            this.msgHandler = msgHandler;
            this.mApiClient = mApiClient;

        }


        private GoogleApiClient getGoogleApiClient() {
            return mApiClient;
        }

        public void sendMessage(final String spath, final byte[] byteStream) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if(!validateGoogleConnection()) {
                            mApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                        }
                        NodeApi.GetConnectedNodesResult result =
                                Wearable.NodeApi.getConnectedNodes(mApiClient).await();
                        List<Node> nodes = result.getNodes();

                        if (nodes.size() > 0) {
                            for(Node node:nodes){
                                Wearable.MessageApi.sendMessage(mApiClient, node.getId(), spath, byteStream);
                                //Log.e(LOG_TAG, "sending to node: " + node.getId());
                            }

                        }
                        else{
                            Log.e(LOG_TAG,"Could not find any connected node.");
                        }
                    }
                }).start();

        }

        private boolean validateGoogleConnection() {
            if (mApiClient == null)
                mApiClient = getGoogleApiClient();
            if (mApiClient.isConnected()){
                return true;
            }
            else
            {
                return false;
            }

        }
            public void terminateConnection(){
                if(mApiClient!=null && (mApiClient.isConnected() || mApiClient.isConnecting())){
                    mApiClient.disconnect();
                }
            }

    }
}

