
package edu.uab.cis.spies.twofactorauthentication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
;
import com.google.android.gms.gcm.GoogleCloudMessaging;


import java.io.IOException;

import edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer.ServerTimeSynchronizer;
import edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer.TimeSynchronizer;
import edu.uab.cis.spies.twofactorauthentication.WebServer.PushToServer;
import edu.uab.cis.spies.twofactorauthentication.gcm.GcmMessageHandler;
import edu.uab.cis.spies.twofactorauthentication.services.SensorValueService;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.service.ServiceMessage;
import edu.uab.cis.spies.twofactorlib.service.ServiceSpecificDetails;
import edu.uab.cis.spies.twofactorlib.service.ServiceState;
import edu.uab.cis.spies.twofactorlib.common.Constants;
/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class MainActivity extends Activity implements OnClickListener{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Button mBtn_getGCMid;
    private EditText mTxtRegId;
    private TextView mTxtInfo,mTxtSender,mTxtReceiver,mTxtHandler,mTxtSync;
    private GoogleCloudMessaging gcm = null;
    private String regid;
    private final String PROJECT_NUMBER = "301468968222";

    private boolean mServiceBound;
    private ServiceSpecificDetails sensorServiceDetails;
    private Intent intentToStartService = null;

    private ServiceConnection sensorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "Sensor Service Connected.");
            mServiceBound = true;
            sensorServiceDetails.setResMessenger(new Messenger(service));
            sensorServiceDetails.setReqMessenger(new Messenger(new ResponseHandler()));
            /*
            * the sole purpose of this message is to initialize replyMessenger in Service.
            * */
            sensorServiceDetails.sendMessage(ServiceMessage.START, Constants.NOTHING,"");
            sensorServiceDetails.setState(ServiceState.CONNECTED);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "Sensor Service Disconneced.");
            sensorServiceDetails.setReqMessenger(null);
            sensorServiceDetails.setResMessenger(null);
            sensorServiceDetails.setState(ServiceState.NOT_CONNECTED);
            mServiceBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initializeView();


    }




    private void initializeView(){
        mBtn_getGCMid = (Button)findViewById(R.id.btnGetRegId);
        mBtn_getGCMid.setOnClickListener(this);
        mTxtRegId = (EditText)findViewById(R.id.etRegId);
        mTxtInfo = (TextView)findViewById(R.id.txtService);
        mTxtSender = (TextView)findViewById(R.id.txtSender);
        mTxtReceiver = (TextView)findViewById(R.id.txtReceiver);
        mTxtHandler = (TextView)findViewById(R.id.txtHandler);
        mTxtSync = (TextView)findViewById(R.id.txtSync);

    }

    @Override
    protected void onPause() {
        super.onPause();
        IntentFilter iff = new IntentFilter(GcmMessageHandler.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice,iff);

    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume()");
        super.onResume();
        IntentFilter iff = new IntentFilter(GcmMessageHandler.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice,iff);

    }


    /* server time synchronizer thread*/
    private ThreadGroup sThreadGroup = new ThreadGroup("2FA MOBILE");
    private FileUtility fileUtility = new FileUtility();
    private ServerTimeSynchronizer serverTimeSynchronizer= new ServerTimeSynchronizer(sThreadGroup, fileUtility);


    /* Initialize and start TimeSynchronizer thread*/
    private void initServerTimeSynchronizer(){
        Log.d(LOG_TAG, "intiServerTimeSynchronizer()");
        if(serverTimeSynchronizer == null){
            serverTimeSynchronizer = new ServerTimeSynchronizer(sThreadGroup, fileUtility);
        }
        if(serverTimeSynchronizer.isAlive()){
            serverTimeSynchronizer.interrupt();
            serverTimeSynchronizer.stop();
            while(serverTimeSynchronizer.isAlive()){
                try{
                    Thread.sleep(200);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
        //serverTimeSynchronizer.setDaemon(true);
        serverTimeSynchronizer.setPriority(Thread.MAX_PRIORITY);
        serverTimeSynchronizer.start();
    }

    private void stopServerTimeSynchronizer(){
        Log.d(LOG_TAG, "stopServerTimeSynchronizer()");
        if(serverTimeSynchronizer!=null){
            Log.d(LOG_TAG, "ServerTimeSYnc is not null");
            if(serverTimeSynchronizer.isAlive()){
                serverTimeSynchronizer.interrupt();

            }
        }
    }

    /* On start of application start the service.*/
    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart()");

        IntentFilter iff = new IntentFilter(GcmMessageHandler.ACTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice,iff);

        //recording directory from web service...
        Intent intent = getIntent();
        String recordingDir = intent.getStringExtra(edu.uab.cis.spies.twofactorauthentication.Constants.RECORDINGS_DIR);
        Log.e(LOG_TAG, "Recording Dir: " + recordingDir);

        if(recordingDir == null){
            recordingDir = String.valueOf(System.currentTimeMillis()) + "_mobile";
        }

//        fileUtility.setWorkingDir(recordingDir);
//        fileUtility.createRecordingFiles();
//        initServerTimeSynchronizer();


        intentToStartService = new Intent(this,SensorValueService.class);
        intentToStartService.putExtra(edu.uab.cis.spies.twofactorauthentication.Constants.RECORDINGS_DIR, recordingDir);
        sensorServiceDetails = new ServiceSpecificDetails(mTxtInfo, intentToStartService);
        startService(intentToStartService);
        bindService(intentToStartService, sensorServiceConnection, Context.BIND_AUTO_CREATE);
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /* on application stop and destroy, stop the service */
    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        stopServerTimeSynchronizer();
        stopServicesIntents();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy()");
        stopServerTimeSynchronizer();
        stopServicesIntents();
        super.onDestroy();
    }

    private void stopServicesIntents(){
        Log.d(LOG_TAG,"stopServicesIntents()");
        if(mServiceBound){
            if(intentToStartService!=null)
                stopService(intentToStartService);
            unbindService(sensorServiceConnection);
            mServiceBound = false;
            sensorServiceDetails.setState(ServiceState.NOT_CONNECTED);
            Log.d(LOG_TAG,"Service bound>>unbindService and stopService invoked.");
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        getRegID();
//        run = !run;
//        if(run){
//            timeSynchronizer = new ServerTimeSynchronizer();
//            timeSynchronizer.setDaemon(true);
//            timeSynchronizer.setPriority(Thread.NORM_PRIORITY);
//            timeSynchronizer.start();
//        }
//        else{
//            if(timeSynchronizer!=null)
//                timeSynchronizer.interrupt();
//        }

//        Intent intent = new Intent(MainActivity.this, NewActivity.class);
//        startActivity(intent);
    }

    public void getRegID() {
        new AsyncTask<Void, Void, String>(){
            @Override
            protected String doInBackground(Void... voids) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    regid = gcm.register(PROJECT_NUMBER);

                    msg = "Registration ID=" + regid;
                    Log.i("GCM", msg);

                    String args[] = {PushToServer.GCMDeviceRegistration, "TwoFactorAuthentication",regid};
                    ThreadGroup tGroup = new ThreadGroup("2FA");
                    PushToServer recordRegisteredDevice = new PushToServer(tGroup, args);
                    recordRegisteredDevice.start();
//                    recordRegisteredDevice.execute(PushToServer.GCMDeviceRegistration, "TwoFactorAuthentication",regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }
            @Override
            protected void onPostExecute(String msg) {
                mTxtRegId.setText(msg + "\n");
            }
        }.execute(null, null, null);
    }



    class ResponseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ServiceMessage serMsg = ServiceMessage.getServiceMsg(msg.what);
            switch(serMsg){
                case START:{
                    Log.d(LOG_TAG,ServiceState.RUNNING.name());
                    sensorServiceDetails.setState(ServiceState.RUNNING);
                    break;
                }
                case STOPPED:
                    Log.d(LOG_TAG,ServiceState.STOPPED.name());
                    sensorServiceDetails.setState(ServiceState.STOPPED);
                    break;
                case SENDER:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    mTxtSender.setText(recData);
                    break;
                }
                case RECEIVER:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    mTxtReceiver.setText(recData);
                    break;
                }
                case HANDLER:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    mTxtHandler.setText(recData);
                    break;
                }
                case TIME_SYNC:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    mTxtSync.setText(recData);
                    break;
                }
                case STOP:{
                    Log.d(LOG_TAG,"Stopping.");
                    sensorServiceDetails.setInfoText("STOPPING.");
                    break;
                }
                case STOP_ACK:{
                    Log.d(LOG_TAG, "STOP_ACK");
                    sensorServiceDetails.setInfoText("STOP_ACK");
                    break;
                }
                case INFO:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    sensorServiceDetails.setInfoText(recData);
                    Log.d(LOG_TAG, recData);
                    break;
                }
                case ERROR:{
                    String err = msg.getData().getString(Constants.ERROR);
                    Log.d(LOG_TAG, "ERROR>>" + err);
                    sensorServiceDetails.setInfoText("ERR:"+err);
                    break;
                }
                case START_ACK:{
                    Log.d(LOG_TAG, "START_ACK");
                    break;
                }
                default:{
                    Log.e(LOG_TAG,"Wrong Service Message: "+serMsg);
                    sensorServiceDetails.setInfoText("Wrong Message Received.");
                    break;
                }
            }
        }
    }

    BroadcastReceiver onNotice = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG,"onReceive()");
            String from = intent.getStringExtra("From");
            String message = intent.getStringExtra("Message");

            Log.d(LOG_TAG, "BroadCast Received:\n" +
                    "From: " + from + " \nMessage: " + message );
            if(message.equalsIgnoreCase("STOP")) {
                stopServerTimeSynchronizer();
            }

        }
    };
}

