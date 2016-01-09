
package edu.uab.cis.spies.twofactorauthentication.wear;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import edu.uab.cis.spies.twofactorauthentication.MainActivity;
import edu.uab.cis.spies.twofactorauthentication.R;
import edu.uab.cis.spies.twofactorlib.common.WearConstants;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class WearListenerService extends WearableListenerService{

    public  final String LOG_TAG = WearListenerService.class.getSimpleName();

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {


    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if( messageEvent.getPath().equalsIgnoreCase( WearConstants.WEAR_START_MSG ) ) {
            Log.d(LOG_TAG,new String(messageEvent.getData()));
            sendLocalNotification(new String(messageEvent.getData()));
            Intent dialogIntent = new Intent(getBaseContext(), MainActivity.class);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(dialogIntent);

        }
        else if(messageEvent.getPath().equalsIgnoreCase( WearConstants.WEAR_STOP_MSG )){
            Log.d(LOG_TAG,new String(messageEvent.getData()));
            sendLocalNotification(new String(messageEvent.getData()));
        }
        super.onMessageReceived(messageEvent);

    }


    private void sendLocalNotification(String info) {
        int notificationId = 001;

        // Create a pending intent that starts this wearable app
        Intent startIntent = new Intent(this, MainActivity.class).setAction(Intent.ACTION_MAIN);
        // Add extra data for app startup or initialization, if available
        startIntent.putExtra("EXTRA", "nothing");
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Two Factor Authentication")
                .setContentText(info)
                .setAutoCancel(true)
                .setContentIntent(startPendingIntent)
                .setVibrate(new long[]{500, 100, 500, 100, 500});

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }




}