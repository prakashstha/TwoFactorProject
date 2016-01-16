package edu.uab.cis.spies.twofactorauthentication.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.uab.cis.spies.twofactorauthentication.Constants;
import edu.uab.cis.spies.twofactorauthentication.MainActivity;
import edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer.ServerTimeSynchronizer;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorauthentication.wear.WearMessageSender;
import edu.uab.cis.spies.twofactorlib.common.GCMCommands;
import edu.uab.cis.spies.twofactorlib.common.WearConstants;

/**
 * <p>
 *     Handle message recieved from Google Cloud Messaging
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class GcmMessageHandler extends IntentService implements GCMCommands
    {
    private static final String LOG_TAG = GcmMessageHandler.class.getSimpleName();
    String msg = "not set yet....";

    private Handler handler;
    private WearMessageSender wearMsgSender;
    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }


    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        // Message from server
        msg = extras.getString("message");
        showToast();
        if(msg!=null)
        {
            if(msg.startsWith(GCM_START))
            {
                String[] str = msg.split(",");
                String directoryName;
                if(str.length == 2){
                    directoryName = str[1];

                    //send msg to wear to initiate sensor recordings
                    initWearService();
                    Log.d("GcmMsgHandler()", "Start Service received -> " + directoryName);

                    Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
                    dialogIntent.putExtra(Constants.RECORDINGS_DIR, directoryName);
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplication().startActivity(dialogIntent);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Mismatched message", Toast.LENGTH_SHORT).show();
                }
            }
            else if(msg.equalsIgnoreCase(GCM_STOP)) {
                stopWearService();
                sendMessageToMainActivity("STOP");
                Log.e("GcmMsgHandler()", "Stop service received");
            }
        }
        Log.i("GCM", "Received : (" + messageType + ")  " + extras.getString("message"));
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    public static final String ACTION="Status";
    public void sendMessageToMainActivity(String msg){
         Intent in = new Intent(ACTION);
         in.putExtra("From", LOG_TAG);
         in.putExtra("Message", msg);
         Log.d(LOG_TAG,"sending BroadCast");
         LocalBroadcastManager.getInstance(this).sendBroadcast(in);

     }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initWearService(){
        Log.d(LOG_TAG,"initWearService()");
        if(wearMsgSender==null)
            wearMsgSender = new WearMessageSender(getBaseContext());
        wearMsgSender.sendWearMessage(WearConstants.WEAR_START_MSG, "starting sensor readings");

    }
    private void stopWearService(){
        //on stop message received from GCM stop wear service
        Log.d(LOG_TAG, "stopWearService()");

        if(wearMsgSender==null)
            wearMsgSender = new WearMessageSender(getBaseContext());
        wearMsgSender.sendWearMessage(WearConstants.WEAR_STOP_MSG,"Stopping sensor readings");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
