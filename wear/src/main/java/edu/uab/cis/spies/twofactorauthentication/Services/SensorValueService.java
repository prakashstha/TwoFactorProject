package edu.uab.cis.spies.twofactorauthentication.Services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.uab.cis.spies.twofactorauthentication.Services.message.handler.MessageHandlerFactory;
import edu.uab.cis.spies.twofactorlib.bo.ReceivedMsgBytes;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.messages.AudioStartTimeInfoMsg;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationRequest;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationResponse;
import edu.uab.cis.spies.twofactorlib.messages.SendSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeRequest;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeResponse;
import edu.uab.cis.spies.twofactorlib.messages.StartRequest;
import edu.uab.cis.spies.twofactorlib.messages.StartResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopRequest;
import edu.uab.cis.spies.twofactorlib.messages.StopResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.WarningMsg;
import edu.uab.cis.spies.twofactorlib.messages.SensorValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.AudioValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandlerFactory;
import edu.uab.cis.spies.twofactorlib.queues.AudioValuesQs;
import edu.uab.cis.spies.twofactorlib.queues.SensorValuesQs;
import edu.uab.cis.spies.twofactorlib.sensor.listeners.AccelerometerListener;
import edu.uab.cis.spies.twofactorlib.threads.AudioRecorder;
import edu.uab.cis.spies.twofactorlib.sensor.listeners.GyroscopeListener;
import edu.uab.cis.spies.twofactorlib.service.ServiceMessage;
import edu.uab.cis.spies.twofactorlib.service.ServiceState;
import edu.uab.cis.spies.twofactorlib.threads.AudioValuesConsumer;
import edu.uab.cis.spies.twofactorlib.threads.MessageReceiver;
import edu.uab.cis.spies.twofactorlib.threads.MessageSender;
import edu.uab.cis.spies.twofactorlib.threads.ReceivedMsgHandler;
import edu.uab.cis.spies.twofactorlib.threads.SensorValuesConsumer;
import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;

import static edu.uab.cis.spies.twofactorlib.common.Util.takeRest;


/**
 * Created by prakashs on 7/24/2015.
 */
public class SensorValueService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,IMessageHandler{
    private static String LOG_TAG = SensorValueService.class.getSimpleName();
    private ThreadGroup sThreadGroup = new ThreadGroup("2FA MOBILE");
    private PowerManager.WakeLock sWakeLock = null;

    //data to send and receive are queued in sOutboundMsgQ and sMsgProcessorQ respectively
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ = new LinkedBlockingQueue<IMessage>();
    private final LinkedBlockingQueue<ReceivedMsgBytes> sMsgProcessorQ = new LinkedBlockingQueue<ReceivedMsgBytes>();

    /*thread to send and receive messages and handler for received message*/
    private MessageReceiver sMsgReceiver = null;
    private MessageSender sMsgSender = null;
    private ReceivedMsgHandler sRcvdMsgHandler = null;

    /*sensor and audio data Qs are temporarily stored in sSensorValueQs and sAudioValueQs respectively*/
    private final SensorValuesQs sSensorValueQs = new SensorValuesQs();
    private final AudioValuesQs sAudioValueQs = new AudioValuesQs();

    /*Sensor and audio listener that stores data on their respective Q's*/
    private final AccelerometerListener sAccListener = new AccelerometerListener(sSensorValueQs);
    private final GyroscopeListener sGyroListener = new GyroscopeListener(sSensorValueQs);
    private AudioRecorder sAudioRecorder = null;

    /**
    * Consumer thread that basically convert the respective data (on their Q's) into appropriate message and
    * push them in the queued to be sent through bluetooth
    */
    private SensorValuesConsumer sSensorValsConsumer = null;
    private AudioValuesConsumer sAudioValsConsumer = null;

    /*
    * Request and response message handler between MainActivity and SensorValueService
    */
    private Messenger sClientMessageHandler = new Messenger(new ClientMessageHandler(this));
    private Messenger sReplyMessenger = null;

    /*Handler for message received over the bluetooth*/
    private final IMessageHandlerFactory sMsgHandlerFactory = new MessageHandlerFactory(this);

    volatile private Channel sBTChannel = null;
    volatile private InputStream sBTInputStream = null;
    volatile private OutputStream sBTOutputStream = null;


    private GoogleApiClient sApiClient;
    private long TIME_OUT_LIMIT = 1000;
    private boolean isStartRespReceived = false, isStartReqSent = false;
    /**
     * Exception handler for threads. When exception occurs
     * in the thread. Service is stopped.
     */
    private final Thread.UncaughtExceptionHandler sThreadExHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            Log.d(LOG_TAG, "Error in thread: " + th.getName());
            Log.d(LOG_TAG, ex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            Log.d(LOG_TAG, sw.toString());
            stopRunningThreads();
            sendMessage(ServiceMessage.STOPPED);
            pw.close();
        }
    };

    /**
     * Initialize GoogleApiClient
     * */
    private void initGoogleApiClient() {
        Log.d(LOG_TAG, "in initGoogleApiClient");
        sApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();
    }
    private void connectGoogleApiClient(){
        if( sApiClient != null && !( sApiClient.isConnected() || sApiClient.isConnecting() ) )
            sApiClient.connect();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                LOG_TAG + "Lock");
        initGoogleApiClient();
        connectGoogleApiClient();
    }

    /*To be safe, connect GoogleApiClient again when Service is bound*/
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        return sClientMessageHandler.getBinder();

    }

    /*To be safe, connect GoogleApiClient again when Service is bound*/
    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
        super.onRebind(intent);
    }

    /*
        onService unbind
        -- Close the channel
        -- Disconnect GoogleAPiClient when service in unbound
    */
    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnBind()>>Service disconneced");
        stopRunningThreads();
        if(sBTChannel!=null && sApiClient.isConnected()){
            sBTChannel.close(sApiClient);
        }
        if(sApiClient!=null)
        {
            if (sApiClient.isConnected() ) {
                sApiClient.disconnect();
            }
        }
        stopSelf();
        return true;
    }

    /**
     *
     * onDestroy do same as on Unbind()
     *   -- Close the channel
     *   -- Disconnect GoogleAPiClient when service in unbound
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sBTChannel!=null && sApiClient.isConnected()){
            sBTChannel.close(sApiClient);
        }
        if(sApiClient!=null)
        {
            if ( sApiClient.isConnected() ) {
                sApiClient.disconnect();
            }
        }

        Log.d(LOG_TAG,"onDestroy()>>Service Destroyed");
        stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");
        return Service.START_REDELIVER_INTENT;
    }

    /**
     * Initialize and start MessageSender Thread
     * */
    private void initMsgSender() {
        Log.d(LOG_TAG,"initMsgSender()");
        sMsgSender = new MessageSender(sThreadGroup, sBTOutputStream,sOutboundMsgQ);
        sMsgSender.setUncaughtExceptionHandler(sThreadExHandler);
        sMsgSender.start();
    }

    /**
     * Initialize and start MessageReceiverThread
     */
    private void initMsgReceiver() {
        Log.d(LOG_TAG,"initMsgReceiver()");
        sMsgReceiver = new MessageReceiver(sThreadGroup, sBTInputStream,
                sMsgProcessorQ);
        sMsgReceiver.setUncaughtExceptionHandler(sThreadExHandler);
        sMsgReceiver.start();
    }

    /**
     * Initialize and start AudioRecorder/AudioValue-producer
     */
    private void initAudioValuesProducer(){
        Log.d(LOG_TAG,"initAudioValuesProducer()");
        sAudioRecorder = new AudioRecorder(sThreadGroup, sAudioValueQs);
        sAudioRecorder.setUncaughtExceptionHandler(sThreadExHandler);
        sendMessage(ServiceMessage.AUDIO, Constants.INFO_TEXT_KEY, ServiceState.RUNNING.name());
        Log.d(LOG_TAG, "Audio Start Time: " + System.currentTimeMillis());
        sAudioRecorder.start();

        int i = 1;
        while(!sAudioRecorder.isTimeSet()){
            takeRest(10);
            Log.d(LOG_TAG,"Waited for: " + (i++)*10 + " ms");
            if(i>200){
                throw new RuntimeException("could not set start time of audio recording");
            }
        }
        sOutboundMsgQ.add(new AudioStartTimeInfoMsg(sAudioRecorder.getStartTimeStamp()));

    }

    /**
     * Initialize and start audio-values-consumer
     * i.e. thread that format the audio data
     */
    private void initAudioValuesConsumer(){
        Log.d(LOG_TAG, "initAudioValueConsumer()");
        sAudioValsConsumer = new AudioValuesConsumer(sThreadGroup,sAudioValueQs, sOutboundMsgQ);
        sAudioValsConsumer.setUncaughtExceptionHandler(sThreadExHandler);
        sAudioValsConsumer.start();
    }

    /**
     * Initialize and start Sensor-data-consume
     * i.e. thread that format sensor data
     */
    private void initSensorValueConsumers() {
        Log.d(LOG_TAG,"initSensorValueConsumers()");
        sSensorValsConsumer = new SensorValuesConsumer(sThreadGroup, sSensorValueQs, sOutboundMsgQ);
        sSensorValsConsumer.setUncaughtExceptionHandler(sThreadExHandler);
        sSensorValsConsumer.start();


    }

    private void registerSensorListener(){
        SensorManager sensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        Sensor accelerometer = sensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sAccListener, accelerometer,
                SensorManager.SENSOR_DELAY_FASTEST);
        sendMessage(ServiceMessage.ACC, Constants.INFO_TEXT_KEY,ServiceState.RUNNING.name());
        Log.d(LOG_TAG, "Accelerometer sensor registered.");

        Sensor gyroscope = sensorManager
                .getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(sGyroListener, gyroscope,
                SensorManager.SENSOR_DELAY_FASTEST);
        sendMessage(ServiceMessage.GYRO, Constants.INFO_TEXT_KEY,ServiceState.RUNNING.name());
        Log.d(LOG_TAG, "Gyroscope sensor registered.");
    }

    private void unregisterSensorListener() {
        SensorManager sensorManager = ((SensorManager) getSystemService(Context.SENSOR_SERVICE));
        if(sAccListener!= null)
        {
            sensorManager.unregisterListener(sAccListener);
            sendMessage(ServiceMessage.ACC,Constants.INFO_TEXT_KEY, ServiceState.NOT_CONNECTED.name());
            Log.d(LOG_TAG, "Accelerometer Sensor unregistered.");
        }
        if(sGyroListener!=null){
            sensorManager.unregisterListener(sGyroListener);
            sendMessage(ServiceMessage.GYRO,Constants.INFO_TEXT_KEY, ServiceState.NOT_CONNECTED.name());
            Log.d(LOG_TAG, "Gyroscope Sensor unregistered.");
        }

    }

    /**
     * Initialize and start Received Message handler thread
     */
    private void initRcvdMsgHandler() {
        Log.d(LOG_TAG, "initRcvdMsgHandler()");
        sRcvdMsgHandler = new ReceivedMsgHandler(sThreadGroup,
                sMsgHandlerFactory, sMsgProcessorQ);
        sRcvdMsgHandler.setUncaughtExceptionHandler(sThreadExHandler);
        sRcvdMsgHandler.start();
    }

    /**
     * call methods that initialize and starts message sender/receiver/handler threads
     */
    private void initMessageThreads(){
        Log.d(LOG_TAG, "initMessageThreads()");
        initMsgSender();
        initMsgReceiver();
        initRcvdMsgHandler();
    }

    /**
    * Initialize and start actual data producer and consumer
    * In our case
    * --->> Producers are: RegisterSensor Listener and AudioRecorder
    * --->> Consumers are: SensorValuesConsumer and AudioValuesConsumer
    */
    private void initDataProducerConsumer(){
        initSensorValueConsumers();
        initAudioValuesConsumer();
        registerSensorListener();
        initAudioValuesProducer();
    }

    /**
    * interrupt all the running threads, wait until all threads are done with their respective jobs
    * SensorListener
    * AudioRecorder
    * SensorValuesConsumer
    * AudioValuesConsumer
    * MessageSender
    * MessageReceiver
    * MessageHandler
    *
    * Close Bluetooth input/output stream
    * Clear all Qs
    * Release wakelock
    */
    public void stopRunningThreads(){
        Log.d(LOG_TAG,"stopRunningThreads()");

        interruptAndStopThreads();

        closeBluetoothChannel();

        Log.e(LOG_TAG, "At end of thread execution>>");
        Log.d(LOG_TAG, "AudioValuesQs: " + sAudioValueQs.getAudioValueQs().size());
        Log.d(LOG_TAG, "SensorValueQs size>> acc:  " +  sSensorValueQs.getAccSegmentersQ().size() + "<<~~>> gyro: "+sSensorValueQs.getGyroSegmentersQ().size());
        Log.d(LOG_TAG, "OutboundMsgQs size: " +  sOutboundMsgQ.size() + " ReceivedMsgProcessorQs size: "+sMsgProcessorQ.size());

        if(sOutboundMsgQ.size()>0){
            for(IMessage msg: sOutboundMsgQ){
                Log.e(LOG_TAG,"Msg Type: " + msg.getMsgType());
            }
        }
        sAudioValueQs.clear();
        sSensorValueQs.clear();
        sOutboundMsgQ.clear();
        sMsgProcessorQ.clear();

        Log.d(LOG_TAG, "Audio,Sensor,Outbound and Received Msg Processor Q's cleared");

        try {
            sWakeLock.release();
            Log.d(LOG_TAG, "WakeLock Released.");
        } catch (RuntimeException re) {
            // It may happen when stop called multiple times from UI
            Log.e(LOG_TAG, "Wakelock is already released.");
        }
    }
    private void interruptAndStopThreads(){
//      interrupt and stop sensro readings
        unregisterSensorListener();

        /*Interrupt all the threads*/

        if (sSensorValsConsumer != null) {
            sSensorValsConsumer.interrupt();
        }

        if(sAudioRecorder!=null){
            Log.d(LOG_TAG,"Audio recorder interrupted:" + System.currentTimeMillis());
            sAudioRecorder.interrupt();
            Log.d(LOG_TAG,"Waiting Audio Recorder Thread....");
            while(sAudioRecorder.isAlive()) {
                takeRest(10);
            }
            Log.d(LOG_TAG, "AudioRecorder Stop:" + System.currentTimeMillis());
            Log.d(LOG_TAG, "Audio Time Info Message send:" + sAudioRecorder.getStartTimeStamp() + "End:" + sAudioRecorder.getEndTimestamp());
            sendMessage(ServiceMessage.AUDIO, Constants.INFO_TEXT_KEY, ServiceState.NOT_CONNECTED.name());
        }

        if(sAudioValsConsumer!=null){
            sAudioValsConsumer.interrupt();
        }


        // Wait while all threads completes their execution
        if (sSensorValsConsumer != null) {
            Log.d(LOG_TAG,"Waiting SensorValuesConsumer Thread....");
            while (sSensorValsConsumer.isAlive()) {
                takeRest(10);
            }
        }

        if(sAudioValsConsumer!=null){
            Log.d(LOG_TAG,"Waiting AudioValuesConsumer Thread....");
            while(sAudioValsConsumer.isAlive()){
                takeRest(10);
            }
        }

        Log.d(LOG_TAG, "stop request added..");
        takeRest(100);
        /* Sender receiver interruption at the ends*/
        if (sMsgSender != null) {
            sMsgSender.interrupt();
        }

        if (sMsgReceiver != null) {
            sMsgReceiver.interrupt();
        }

        if (sRcvdMsgHandler != null) {
            sRcvdMsgHandler.interrupt();
        }


        if (sMsgSender != null) {
            while (sMsgSender.isAlive()) {
                takeRest(10);
            }
        }
        if (sMsgReceiver != null) {
            while (sMsgReceiver.isAlive()) {
                takeRest(10);
            }
        }
        if (sRcvdMsgHandler != null) {
            while (sRcvdMsgHandler.isAlive()) {
                takeRest(10);
            }
        }
        Log.d(LOG_TAG, "AudioRecorder, Sensor/Audio vals Consumer, Msg Sender/Receiver/Handler threads complete their executions.");
        sendMessage("Threads completed jobs");
    }

    private void closeBluetoothChannel(){
        int count = 0;
        if (sBTOutputStream != null) {
            try {
                sBTOutputStream.close();
                sBTOutputStream = null;
                count++;
                Log.d(LOG_TAG, "Bluetooth output-stream closed");

            } catch (IOException ioe) {
                Log.e(LOG_TAG, "Failed to close Bluetooth output stream : "
                        + ioe.getMessage());
                sendMessage(ServiceMessage.ERROR, Constants.ERROR, "BTo Closing Error");

            }
        }
        if (sBTInputStream != null) {
            try {
                sBTInputStream.close();
                sBTInputStream = null;
                count++;
                Log.d(LOG_TAG, "Bluetooth input-stream closed");
            } catch (IOException ioe) {
                sendMessage(ServiceMessage.ERROR, Constants.ERROR, "BTi Closing Error");
                Log.e(LOG_TAG, "Failed to close Bluetooth input stream : "
                        + ioe.getMessage());
            }
        }
        if(count == 2){
            sendMessage("BT Channel Closed");
        }
    }
    /**
     * Initialize Bluetooth channel for message transmission
     */
    private void initBtChannel() {
        Thread initCommunicationChannel = new InitCommunicationChannel(sThreadGroup);
        initCommunicationChannel.setPriority(Thread.NORM_PRIORITY);
        initCommunicationChannel.setDaemon(true);
        initCommunicationChannel.start();
        int i = 0;
        /*
        * Wait for 5 second to initailize bluetooth channel
        * If it fails to initialize within timelimit, throw error
        */
        while(initCommunicationChannel.isAlive())
        {
            Log.d(LOG_TAG,"resting for 1000 ms");
            takeRest(1000);
            i++;
            if(i>5) {
               // throw new RuntimeException("Could not initialize bluetooth channel");
                Log.e(LOG_TAG,"Could not initiallize bluetooth channel.");

                break;
            }
        }
        Log.d(LOG_TAG, "Initialized bluetooth channel");

    }


    /**
     * Validate Google Api Client Connection
     */
    private boolean validateGoogleClientConnection(){
        if(sApiClient!=null && sApiClient.isConnected())
            return true;

        if(sApiClient == null) {
            initGoogleApiClient();
        }

        int i = 0;
        if(sApiClient.isConnecting()){
            while(!sApiClient.isConnected()){
                takeRest(10);
                i++;
                if(i>10){
                    Log.e(LOG_TAG,"Time-out connecting GoogleApiClient");
                    break;
                }
            }
        }else if(!sApiClient.isConnected()) {
            sApiClient.blockingConnect(TIME_OUT_LIMIT, TimeUnit.MILLISECONDS);
        }
        if(sApiClient.isConnected())
            return true;
        else
            return false;
    }

    /**
     * Thread that initialize bluetooth input/output stream
     */
    private class InitCommunicationChannel extends TwoFactorThread{
        private String LOCAL_TAG = LOG_TAG + "::" + InitCommunicationChannel.class.getSimpleName();
        public InitCommunicationChannel(ThreadGroup tGroup){
            super(tGroup, "InitCommunicationChannel");
        }
        @Override
        public void mainloop() {
            Log.d(LOCAL_TAG, ThreadStatus.START);
            //connect to GoogleApiClient and find connected nodes
            if(!validateGoogleClientConnection()){
                Log.e(LOCAL_TAG,"Could not connect to Google Api Client");
                sendMessage(ServiceMessage.ERROR, Constants.ERROR,"Time-out connecting GoggleAPI");
                throw new RuntimeException("Could not connect to Google Api Client.");
            }

            NodeApi.GetConnectedNodesResult result = Wearable.NodeApi.getConnectedNodes(sApiClient).await(TIME_OUT_LIMIT, TimeUnit.MILLISECONDS);
            List<Node> nodes = result.getNodes();
            if(nodes.size()<=0)
            {
                Log.d(LOCAL_TAG,"No connected node found.");
                sendMessage(ServiceMessage.ERROR, Constants.ERROR, "No connected node found.");
                throw new RuntimeException("No connected node found.");
            }
            Log.d(LOCAL_TAG, ThreadStatus.RUNNING);

            boolean success = false;
            for (Node node: nodes) {
                Log.d(LOG_TAG,"Node id: " + node.getDisplayName());

                ChannelApi.OpenChannelResult res = Wearable.ChannelApi.openChannel(sApiClient,node.getId(), Constants.MSG).await(TIME_OUT_LIMIT, TimeUnit.MILLISECONDS);
                sBTChannel = res.getChannel();
                if(sBTChannel == null){
                    continue;
                }else{
                    Log.d(LOCAL_TAG,"Channel opened...");
                    success = true;
                }

                sBTInputStream = sBTChannel.getInputStream(sApiClient).await().getInputStream();
                sBTOutputStream = sBTChannel.getOutputStream(sApiClient).await().getOutputStream();

                Log.d(LOCAL_TAG, "Bluetooth channel initialization succeed");
                if(sBTInputStream == null) {
                    Log.e(LOCAL_TAG, "Bluetooth InputStream initialization failed.");
                    throw new RuntimeException("Bluetooth InputStream initialization failed.");
                }
                else
                    Log.d(LOCAL_TAG,"Bluetooth InputStream initialization succeed.");

                if(sBTOutputStream == null) {
                    Log.e(LOCAL_TAG, "Bluetooth OutputStream initialization failed.");
                    throw new RuntimeException("Bluetooth OutputStream initialization failed.");
                }
                else
                    Log.d(LOCAL_TAG,"Bluetooth OutputStream initialization succeed.");

                sendMessage("Bluetooth Channel Initialized");
                success = true;
                if(success)
                    break;
            }
            if(sBTChannel == null) {
                Log.e(LOCAL_TAG, "Bluetooth channel initialization failed");
                throw new RuntimeException("Bluetooth channel initialization failed");
            }

    }
}

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Google API connection succeed....");
        //initialize channel
        initBtChannel();
        initMessageThreads();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"onConnectionSuspended...." + i);
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed >>" + connectionResult.toString());

    }



    private void vibrate() {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(800);
    }


    /* MainActivity to Service Message Handler */
    class ClientMessageHandler extends Handler{

        private final String LOCAL_TAG = LOG_TAG + "::"+ClientMessageHandler.class.getSimpleName();
        private  SensorValueService sService;

        public ClientMessageHandler(SensorValueService sService)
        {
            this.sService = sService;
        }

        @Override
        public void handleMessage(Message msg) {
            sService.sReplyMessenger = msg.replyTo;
            ServiceMessage serMsg = ServiceMessage.getServiceMsg(msg.what);
            switch (serMsg){
                case START:{
                    Log.d(LOCAL_TAG, "START");
                    sOutboundMsgQ.add(new StartRequest());
                    if(!isStartRespReceived && isStartReqSent) {
                        initBtChannel();
                        initMessageThreads();
                    }
                    isStartReqSent = true;
                    break;
                    }
                case STOP:{
                    Log.d(LOCAL_TAG, "STOP");
                    sService.sendMessage(ServiceMessage.STOP_ACK);
                    sOutboundMsgQ.add(new StopRequest());
                    takeRest(200);
                    sService.stopRunningThreads();
                    sService.sendMessage(ServiceMessage.STOPPED);
                    break;
                }
                case START_ACK:
                case STOP_ACK:
                case INFO:
                case ERROR:
                    Log.e(LOG_TAG,"Unexpected Service Message: " + serMsg.name());
                    new IllegalArgumentException("Unexpected Service Message: " + serMsg.name());
                    break;
                default:{
                    Log.e(LOCAL_TAG,"Wrong Service Message.");
                    new IllegalArgumentException("Wrong Service Message: " + serMsg.name());
                    break;
                }

            }
        }
    }
 /**
  * This service also acts as Received message handler,
  * overriding method handle()
  */
    @Override
    public void handle(IMessage msg) {
        try {
            handleMsg(msg);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    /**
     *  Method that handles messages received from connected nodes
     */
    public void handleMsg(IMessage msg) throws InterruptedException {
        switch (msg.getMsgType()) {
            case StartResponse.MSG_TYPE:
                Log.d(LOG_TAG,"StartResponse");
                isStartRespReceived = true;
                //init values consumer and then start producer
                initDataProducerConsumer();
                //notify mainActivity that service started working
                sendMessage(ServiceMessage.START_ACK);
                break;
            case StopResponse.MSG_TYPE:
                isStartReqSent = false;
                isStartRespReceived = false;
                Log.d(LOG_TAG, "StopResponse");
                break;
            case SendSensorValuesRequest.MSG_TYPE:
                Log.d(LOG_TAG, "SendSensorValuesRequest");
                break;
            case StopSensorValuesRequest.MSG_TYPE:
                Log.d(LOG_TAG, "StopSensorValueRequest");
                break;
            case WarningMsg.MSG_TYPE:
                Log.w(LOG_TAG,"WarningMsg");
                vibrate();
                break;
            case RTTCalculationRequest.MSG_TYPE:
                sOutboundMsgQ.add(new RTTCalculationResponse(msg));
                break;
            case ShareTimeRequest.MSG_TYPE:
                sOutboundMsgQ.add(new ShareTimeResponse(msg));
                break;
            case SensorValuesMsg.MSG_TYPE:
            case AudioValuesMsg.MSG_TYPE:
            case StartRequest.MSG_TYPE:
            case StopRequest.MSG_TYPE:
            case RTTCalculationResponse.MSG_TYPE:
            case ShareTimeResponse.MSG_TYPE:
                Log.e(LOG_TAG,"Unexpected Message Type: " + msg.getMsgType());
                new IllegalArgumentException("Unexpected Message Type: " + msg.getMsgType());
                break;
            default:
                Log.e(LOG_TAG, "Incorrect Message");
                new IllegalArgumentException("Incorrect Message");

        }
    }
    private void sendMessage(String msg) {
        sendMessage(ServiceMessage.INFO, Constants.INFO_TEXT_KEY, msg);
    }

    public void sendMessage(ServiceMessage what) {
        sendMessage(what, Constants.NOTHING, "");
    }

    public void sendMessage(ServiceMessage what, String key, String extra) {
        if (sReplyMessenger == null) {
            Log.e(LOG_TAG,"Reply Messenger is null");
            return;
        }
        Message msg = Message.obtain(null, what.ordinal());
        Bundle msgBundle = new Bundle();
        msgBundle.putString(key, extra);
        msg.setData(msgBundle);
        try {
            sReplyMessenger.send(msg);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Sending message to main activity failed");
        }
    }




}
