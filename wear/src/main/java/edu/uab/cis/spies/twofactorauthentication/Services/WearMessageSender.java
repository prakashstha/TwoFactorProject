package edu.uab.cis.spies.twofactorauthentication.Services;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.uab.cis.spies.twofactorlib.common.WearConstants;

/**
 * Created by prakashs on 8/13/2015.
 */
public class WearMessageSender implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private final String LOG_TAG = WearMessageSender.class.getSimpleName();
    private GoogleApiClient googleClient;
    private Context context;
    private long CONNECTION_TIME_OUT_MS = 1000;


    // Constructor for sending data objects to the data layer
    public WearMessageSender(Context context) {
        this.context = context;
        initGoogleApiClient();
    }

    private void initGoogleApiClient(){
        // Build a new GoogleApiClient for the the Wearable API
        googleClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }
    private boolean validateGoogleApiClient(){
        if(googleClient.isConnected())
            return true;
        else{
            googleClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
            if(googleClient.isConnected())
                return true;
            else
                return false;
        }

    }

    public void sendWearMessage(String msg){
        sendWearMessage(WearConstants.WEARABLE_DATA_PATH, msg);

    }
    public void sendWearMessage(String path, String msg){
        Log.d(LOG_TAG,"sendWearMessage("+path+","+msg+")");
        new SenderThread(path,msg).start();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "onConnected()");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"onConnectionSuspended()");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed()");
    }

    class SenderThread extends Thread{
        String path, value;
        String local_tag = "SenderThread";
        public SenderThread(String path, String message){
            this.path = path;
            this.value = message;
        }
        public void run() {
            Log.d(local_tag, "run()");
            if(validateGoogleApiClient())
            {
                Log.d(local_tag, "validateGoogleApiClient() success");
               // googleClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(googleClient).await();
                List<Node> nodes = result.getNodes();
                Log.d(LOG_TAG, "outside loop" + nodes.size());
                for(Node node: nodes){
                    Wearable.MessageApi.sendMessage(googleClient, node.getId(), path, value.getBytes());
                    Log.d(local_tag, "send to node "+node.getId());
                }
            }
            else{
                throw new IllegalArgumentException("Cannot connect google api client");
            }
            Log.d(LOG_TAG,"end of thread");
        }
    }

}