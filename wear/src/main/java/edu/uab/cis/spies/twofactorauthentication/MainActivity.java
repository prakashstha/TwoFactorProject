package edu.uab.cis.spies.twofactorauthentication;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import edu.uab.cis.spies.twofactorauthentication.Services.SensorValueService;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.service.ServiceMessage;
import edu.uab.cis.spies.twofactorlib.service.ServiceSpecificDetails;
import edu.uab.cis.spies.twofactorlib.service.ServiceState;


public class MainActivity extends Activity{

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private Button wBtnStart, wBtnStop;
    private TextView wTxtAcc, wTxtGyro,wTxtAudio,wTxtViewInfo, wTxtService;
    private Boolean isStart = false, isStop = true;
    private ServiceSpecificDetails sensorServiceDetails;
    private boolean wServiceBound = false, firstRun = true;
    private SensorResponseHandler sensorResponseHandler = new SensorResponseHandler();
    private Intent intentToStartService = null;



    private ServiceConnection sensorServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(LOG_TAG, "Service Connected.");
            wServiceBound = true;
            sensorServiceDetails.setResMessenger(new Messenger(service));
            sensorServiceDetails.setReqMessenger(new Messenger(sensorResponseHandler));
            sensorServiceDetails.setState(ServiceState.CONNECTED);
            sendMessage(ServiceMessage.START);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(LOG_TAG, "Sensor Service Disconneced.");
            sensorServiceDetails.setReqMessenger(null);
            sensorServiceDetails.setResMessenger(null);
            sensorServiceDetails.setState(ServiceState.NOT_CONNECTED);
            wServiceBound = false;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.round_activity_main);
        initializeView();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        startServiceIntents();
    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initializeView() {

        wBtnStart = (Button)findViewById(R.id.wStartButton);
        wBtnStop = (Button) findViewById(R.id.wStopButton);
        wTxtViewInfo = (TextView) findViewById(R.id.txtInfo);
        wTxtAcc = (TextView)findViewById(R.id.txtAcc);
        wTxtGyro = (TextView)findViewById(R.id.txtGyro);
        wTxtAudio = (TextView)findViewById(R.id.txtAudio);
        wTxtService = (TextView)findViewById(R.id.txtService);
        changeButtonStatus();

        wBtnStart.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startServices();
            }
        });

        wBtnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                stopServices();

            }
        });

    }
    private void startServices(){
        if(wServiceBound){
            firstRun = false;
            Log.d(LOG_TAG,"Service already connected.");
            sendMessage(ServiceMessage.START);
        }
        else{
            startServiceIntents();
        }
    }
    private void stopServices(){
        if(wServiceBound){
            sendMessage(ServiceMessage.STOP);
            Log.d(LOG_TAG,"Stopping service request sent.");
            sensorServiceDetails.setInfoText("STOPPING_req");
        }
        else
        {
            Log.d(LOG_TAG,"Service not connected");
            sensorServiceDetails.setState(ServiceState.NOT_CONNECTED);

        }

    }

    private void stopServicesIntents(){
        Log.d(LOG_TAG, "stopServicesIntents()");

        if(wServiceBound){
            if(intentToStartService!=null)
                stopService(intentToStartService);
            unbindService(sensorServiceConnection);
            Log.d(LOG_TAG,"Service bound>>unbindService and stopService invoked.");
            wServiceBound = false;
            //sensorServiceDetails.setState(ServiceState.NOT_CONNECTED);
        }
        else{
            Log.d(LOG_TAG,"Service not bound");
        }


    }

    private void startServiceIntents(){
        Log.d(LOG_TAG,"startServiceIntents()");
        intentToStartService = new Intent(this,SensorValueService.class);
        sensorServiceDetails = new ServiceSpecificDetails(wTxtService, intentToStartService);
        startService(intentToStartService);
        bindService(intentToStartService, sensorServiceConnection, Context.BIND_AUTO_CREATE);

    }

    private void changeButtonStatus() {
        if(isStart)
            wBtnStart.setEnabled(false);
        else
            wBtnStart.setEnabled(true);
        if(isStop)
            wBtnStop.setEnabled(false);
        else
            wBtnStop.setEnabled(true);

    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG,"onResume()");
        super.onResume();
        IntentFilter iff = new IntentFilter(WearListenerService.INSTRUCTION);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,iff);
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG,"onPause()");
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
//        stopServices();
//        stopServicesIntents();
        super.onPause();

    }


    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        stopServices();
        stopServicesIntents();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        //stopServices();
        Log.d(LOG_TAG, "onDestroy()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        stopServices();
        stopServicesIntents();
        super.onDestroy();

    }

    private void sendMessage(String msg) {
        sendMessage(ServiceMessage.INFO, Constants.INFO_TEXT_KEY, msg);
    }

    public void sendMessage(ServiceMessage what) {
        sendMessage(what, Constants.NOTHING, "");
    }

    public void sendMessage(ServiceMessage what, String key, String extra) {

              sensorServiceDetails.sendMessage(what, key, extra);

    }

    class SensorResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            ServiceMessage serMsg = ServiceMessage.getServiceMsg(msg.what);
            switch(serMsg){

                case START_ACK:{
                    Log.d(LOG_TAG, "Service started working...");
                    sensorServiceDetails.setState(ServiceState.RUNNING);
                    isStart = true;
                    isStop = false;
                    changeButtonStatus();
                    break;
                }
                case STOP_ACK:{
                    Log.d(LOG_TAG, "Service stopping response received.");
                    sensorServiceDetails.setInfoText("STOPPING_RES");
                    isStart = false;
                    isStop = true;
                    changeButtonStatus();
                    break;
                }
                case STOPPED:{
                    Log.d(LOG_TAG,"STOPPED_res");
                    sensorServiceDetails.setState(ServiceState.STOPPED);
                    //stopServicesIntents();
                    isStart = false;
                    isStop = true;
                    changeButtonStatus();
                    break;
                }
                case INFO:{
                    String recData = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    wTxtViewInfo.setText(recData);
                    Log.d(LOG_TAG, recData);
                    break;
                }

                case ERROR:{
                    String err = msg.getData().getString(Constants.ERROR);
                    Log.e(LOG_TAG, "ERROR>>" + err);
                    wTxtViewInfo.setText("ERR>>" + err);
                    break;
                }
                case ACC:{
                    String info = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    wTxtAcc.setText(info);
                    break;
                }
                case GYRO:{
                    String info = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    wTxtGyro.setText(info);
                    break;
                }
                case AUDIO:
                {
                    String info = msg.getData().getString(Constants.INFO_TEXT_KEY);
                    wTxtAudio.setText(info);
                    break;
                }

                case STOP:
                    sensorServiceDetails.setInfoText("Exception in Service.");
                    break;
                case START:
                default:{
                    Log.e(LOG_TAG,"Wrong Service Message"+serMsg);
                    wTxtViewInfo.setText("Wrong Message Received");
                    break;
                }

            }
        }
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive() BroadCastReceiver...");
            String from = intent.getStringExtra("From");
            String message = intent.getStringExtra("Message");
            Log.d(LOG_TAG,"From: " +  from + " Message: "+ message);
            if(message.equalsIgnoreCase("Stop")){
                sendMessage(ServiceMessage.STOP);
            }
        }
    };
}