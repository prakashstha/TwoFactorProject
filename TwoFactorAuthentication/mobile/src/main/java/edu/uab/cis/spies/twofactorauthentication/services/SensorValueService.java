package edu.uab.cis.spies.twofactorauthentication.services;

import android.app.Service;
import android.content.Intent;
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
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Channel;
import com.google.android.gms.wearable.ChannelApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer.TimeSynchronizer;
import edu.uab.cis.spies.twofactorauthentication.handler.AudioTimeInfoMsgHandler;
import edu.uab.cis.spies.twofactorauthentication.handler.AudioValuesMsgHandler;
import edu.uab.cis.spies.twofactorauthentication.handler.MessageHandlerFactory;
import edu.uab.cis.spies.twofactorauthentication.handler.SensorValuesMsgHandler;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.bo.ReceivedMsgBytes;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.SendSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.StartRequest;
import edu.uab.cis.spies.twofactorlib.messages.StartResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopRequest;
import edu.uab.cis.spies.twofactorlib.messages.StopResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.WarningMsg;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandlerFactory;
import edu.uab.cis.spies.twofactorlib.service.ServiceMessage;
import edu.uab.cis.spies.twofactorlib.service.ServiceState;
import edu.uab.cis.spies.twofactorlib.threads.AudioPlayer;
import edu.uab.cis.spies.twofactorlib.threads.MessageReceiver;
import edu.uab.cis.spies.twofactorlib.threads.MessageSender;
import edu.uab.cis.spies.twofactorlib.threads.ReceivedMsgHandler;
import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;

import static edu.uab.cis.spies.twofactorlib.common.Util.takeRest;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValueService extends Service implements ChannelApi.ChannelListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,IMessageHandler{
    private static String LOG_TAG = SensorValueService.class.getSimpleName();
    private ThreadGroup sThreadGroup = new ThreadGroup("2FA MOBILE");
    private PowerManager.WakeLock sWakeLock = null;
    private long TIME_OUT_LIMIT = 1000;
    private boolean isStopThreadInvoked = false;

    /* Queueing messages to-send/received*/
    private final LinkedBlockingQueue<IMessage> sOutboundMsgQ = new LinkedBlockingQueue<IMessage>();
    private final LinkedBlockingQueue<ReceivedMsgBytes> sMsgProcessorQ = new LinkedBlockingQueue<ReceivedMsgBytes>();

    private MessageSender sMsgSender = null; //Thread that sends messages to connected wearable nodes
    private MessageReceiver sMsgReceiver = null; //Thread that receives messages from connected wearable nodes
    private ReceivedMsgHandler sRcvdMsgHandler = null; //Thread that handle received messages

    private FileUtility fileUtility = new FileUtility();

    /* Time synhronizer thread*/
    private final TimeSynchronizer sTimeSynchronizer = new TimeSynchronizer(sThreadGroup, sOutboundMsgQ, fileUtility);

    /* Messenger to communicate with mainActivity and service*/
    private Messenger sClientMessageHandler = new Messenger(new ClientMessageHandler(this));
    private Messenger sReplyMessenger = null;

    // Initialize message handlers
    private final IMessageHandler sInstructionMsgHandler = SensorValueService.this;  //handles instruction messages
    private final IMessageHandler sSensorValsMsgHandler = new SensorValuesMsgHandler(fileUtility, sTimeSynchronizer); //handle sensor value messages
    private final IMessageHandler sAudioValsMsgHandler = new AudioValuesMsgHandler(fileUtility); //handle audio values messages
    private final IMessageHandler sAudioTimeInfoMsgHandler = new AudioTimeInfoMsgHandler(fileUtility);
    private final IMessageHandler sTimeSynchronizerMsgHandler = (IMessageHandler)sTimeSynchronizer.getWearTimeSynchronizer();//handle time synchronizing messages;
    private final IMessageHandlerFactory sMsgHandlerFactory = new MessageHandlerFactory(sInstructionMsgHandler, sSensorValsMsgHandler,sAudioValsMsgHandler,sAudioTimeInfoMsgHandler, sTimeSynchronizerMsgHandler);

    /* Communication channel to communicate with nodes*/
    volatile private Channel sBTChannel = null;
    volatile private InputStream sBTInputStream = null;
    volatile private  OutputStream sBTOutputStream = null;
    private GoogleApiClient sApiClient;
    private AudioPlayer player = null;
    private PhoneAudioRecorder phoneAudioRecorder= null;
    private String recordingDir = "";


    /**
     * Exception handler for threads. When exception occurs
     * in the thread. Service is stopped.
     */
    private final Thread.UncaughtExceptionHandler sThreadExHandler = new Thread.UncaughtExceptionHandler() {
        public void uncaughtException(Thread th, Throwable ex) {
            Log.e(LOG_TAG, "Error in thread: " + th.getName());
            Log.e(LOG_TAG, ex.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            ex.printStackTrace(pw);
            Log.e(LOG_TAG, sw.toString());
            stopRunningThreads();
            sendMessage(ServiceMessage.ERROR, Constants.ERROR, "Exception");
            pw.close();
        }
    };

    private void initGoogleApiClient() {
        Log.d(LOG_TAG, "initGoogleApiClient()");
        sApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "onCreate()");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        sWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                LOG_TAG + "Lock");

    }

    /*To be safe, connect GoogleApiClient again when Service is bound*/
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "onBind()");
        if( sApiClient != null && !( sApiClient.isConnected() || sApiClient.isConnecting() ) )
            sApiClient.connect();
        return sClientMessageHandler.getBinder();

    }

     /*To be safe, connect GoogleApiClient again when Service is bound*/
    @Override
    public void onRebind(Intent intent) {
        Log.d(LOG_TAG, "onRebind()");
       if( sApiClient != null && !( sApiClient.isConnected() || sApiClient.isConnecting() ) )
            sApiClient.connect();

        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(LOG_TAG, "onUnBind()");
        stopRunningThreads();
        //msgSender.sendMessage(Constants.APP_STOP, "wear app stopped".getBytes());
        //Log.d(LOG_TAG, "app, stop....");
        if(sApiClient!=null)
        {
            //Wearable.MessageApi.removeListener( sApiClient, this );
            if ( sApiClient.isConnected() ) {
                sApiClient.disconnect();
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        if(sBTChannel!=null && sApiClient.isConnected()){
            sBTChannel.close(sApiClient);
        }
        if(sApiClient!=null)
        {
            if ( sApiClient.isConnected() ) {
                sApiClient.disconnect();
            }
        }
        Log.d(LOG_TAG, "onDestroy()>>Service Destroyed");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand()");

        recordingDir = intent.getStringExtra(edu.uab.cis.spies.twofactorauthentication.Constants.RECORDINGS_DIR);
        if(recordingDir==null) {
            Log.e(LOG_TAG, "Recording dir is null");
            recordingDir = "";
        }
        fileUtility.setWorkingDir(recordingDir);
        initGoogleApiClient();
        return Service.START_REDELIVER_INTENT;

    }

    /* Initialize and start TimeSynchronizer thread*/
    private void initTimeSynchronizer(){
        Log.d(LOG_TAG, "intiTimeSynchronizer()");
        sTimeSynchronizer.setUncaughtExceptionHandler(sThreadExHandler);
        sTimeSynchronizer.startTimeSync();

        if(sTimeSynchronizer.isAlive())
            sendMessage(ServiceMessage.TIME_SYNC, Constants.INFO_TEXT_KEY,"RUNNING");
    }

    /* Initialize and start MessageSender thread*/
    private void initMsgSender() {
        Log.d(LOG_TAG,"initMsgSender()");
        sMsgSender = new MessageSender(sThreadGroup, sBTOutputStream,sOutboundMsgQ);
        sMsgSender.setUncaughtExceptionHandler(sThreadExHandler);
        sMsgSender.start();
        if(sMsgSender.isAlive())
            sendMessage(ServiceMessage.SENDER, Constants.INFO_TEXT_KEY,"RUNNING");
    }

    /* Initialize and start MessageReceiver thread*/
    private void initMsgReceiver() {
        Log.d(LOG_TAG, "initMsgReceiver()");
        sMsgReceiver = new MessageReceiver(sThreadGroup, sBTInputStream,
                sMsgProcessorQ);
        sMsgReceiver.setUncaughtExceptionHandler(sThreadExHandler);
        sMsgReceiver.start();
        if(sMsgReceiver.isAlive())
            sendMessage(ServiceMessage.RECEIVER, Constants.INFO_TEXT_KEY,"RUNNING");
    }

    /* Initialize and start ReceivedMessageHandler thread*/
    private void initRcvdMsgHandler() {
        Log.d(LOG_TAG,"initRcvdMsgHandler()");
        sRcvdMsgHandler = new ReceivedMsgHandler(sThreadGroup,
                sMsgHandlerFactory, sMsgProcessorQ);
        sRcvdMsgHandler.setUncaughtExceptionHandler(sThreadExHandler);
        sRcvdMsgHandler.start();
        if(sRcvdMsgHandler.isAlive())
            sendMessage(ServiceMessage.HANDLER, Constants.INFO_TEXT_KEY,"RUNNING");
    }

    /* call methods that initialize and starts message sender/receiver/handler threads*/
    public void initMessageThreads(){
        Log.d(LOG_TAG, "initMessageThreads()");
        initMsgSender();
        initMsgReceiver();
        initRcvdMsgHandler();
    }

    /*
    * Interrupt all running threads and then wait for each threads to finish their jobs
    * Threads are==>> MsgSender, MsgReceiver, RcvdMsgHandler, TimeSynchronizer
    * Close Bluetooth input/output stream/channel
    * Clear Q's
    * Release wake lock
    */
    public void stopRunningThreads(){

        Log.d(LOG_TAG,"stopRunningThreads()");

        /*Play audio while stopping all running threads*/
        if(player!=null)
        {
            player.start();
            sendMessage("Audio Player Started.");
            player = null;
        }
        if(phoneAudioRecorder!=null){
            phoneAudioRecorder.setRecording(false);
            sendMessage("Audio Recorder Stopped...");
            phoneAudioRecorder = null;
        }
        sendMessage(ServiceMessage.STOP);


        /*Interrupt all the threads*/
        if (sMsgSender != null) {
            sMsgSender.interrupt();
        }
        sendMessage(ServiceMessage.SENDER, Constants.INFO_TEXT_KEY, ServiceState.INTERRUPTED.name());

        if (sMsgReceiver != null) {
            sMsgReceiver.interrupt();
        }
        sendMessage(ServiceMessage.RECEIVER, Constants.INFO_TEXT_KEY,ServiceState.INTERRUPTED.name());

        if (sRcvdMsgHandler != null) {
            sRcvdMsgHandler.interrupt();
        }
        sendMessage(ServiceMessage.HANDLER, Constants.INFO_TEXT_KEY,ServiceState.INTERRUPTED.name());
        
        if(sTimeSynchronizer!=null) {
            sTimeSynchronizer.interruptTimeSync();
        }
        sendMessage(ServiceMessage.TIME_SYNC, Constants.INFO_TEXT_KEY,ServiceState.INTERRUPTED.name());
        
        Log.d(LOG_TAG, "Msg Sender/Receiver/Handler and TimeSynchronizer threads are interrupted");
        //sendMessage("Threads Interrupted.");

        // Wait while all threads completes their execution
        if (sMsgSender != null) {
            while (sMsgSender.isAlive()) {
                takeRest(10);
            }
        }
        sendMessage(ServiceMessage.SENDER, Constants.INFO_TEXT_KEY, ServiceState.STOPPED.name());
        
        if (sMsgReceiver != null) {
            while (sMsgReceiver.isAlive()) {
                takeRest(10);
            }
        }
        sendMessage(ServiceMessage.RECEIVER, Constants.INFO_TEXT_KEY, ServiceState.STOPPED.name());

        if (sRcvdMsgHandler != null) {
            while (sRcvdMsgHandler.isAlive()) {
                takeRest(10);
            }
        }
        sendMessage(ServiceMessage.HANDLER, Constants.INFO_TEXT_KEY, ServiceState.STOPPED.name());

        if(sTimeSynchronizer != null) {
            if(sTimeSynchronizer.getServerTimeSynchronizer()!=null){
                while(sTimeSynchronizer.getServerTimeSynchronizer().isAlive()){
                    takeRest(10);
                }
            }

            if(sTimeSynchronizer.getWearTimeSynchronizer()!=null){
                while(sTimeSynchronizer.getWearTimeSynchronizer().isAlive()){
                    takeRest(10);
                }
            }
        }
        sendMessage(ServiceMessage.TIME_SYNC, Constants.INFO_TEXT_KEY, ServiceState.STOPPED.name());

        Log.d(LOG_TAG, "Msg Sender/Receiver/Handler, TimeSynchronizer threads complete their executions.");

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

        Log.e(LOG_TAG, "At end of thread execution>>");
        Log.d(LOG_TAG, "OutboundMsgQs size: " + sOutboundMsgQ.size() + " ReceivedMsgProcessorQs size: " + sMsgProcessorQ.size());
        if(sOutboundMsgQ.size()>0){
            for(IMessage msg: sOutboundMsgQ){
                Log.e(LOG_TAG,"Msg Type: " + msg.getMsgType());
            }
        }
        sMsgProcessorQ.clear();
        sOutboundMsgQ.clear();

        try {
            sWakeLock.release();
            Log.d(LOG_TAG, "Released wakelock");
        } catch (RuntimeException re) {
            // It may happen when stop called multiple times from UI
            Log.e(LOG_TAG, "Wakelock is already released");
        }

        /*wait while audio player playing*/
        if(player!=null){
            sendMessage("Audio player still playing.");
            while (player.isAlive()) {
                takeRest(1000);
                Log.d(LOG_TAG, "Playing");
            }
            sendMessage("Audio Player stopped.");
        }
        else{
            sendMessage("Audio Player stopped.");
        }


    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.d(LOG_TAG, "Google API connection succeed....");
        Wearable.ChannelApi.addListener(sApiClient, this);
        Log.d(LOG_TAG, "Channel Listener added.");

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG,"GoogleApi onConnectionSuspended():" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "Google API onConnectionFailed() >>" + connectionResult.toString());
    }

    private void vibrate() {
        ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(800);
    }


    @Override
    public void onChannelOpened(Channel channel) {
        Log.d(LOG_TAG, "onChannelOpened()");
        sendMessage("Channel Opened");
        sBTChannel = channel;

        fileUtility.createRecordingFiles();
        initBtChannel();
        initMessageThreads();
        initTimeSynchronizer();

        //player = new AudioPlayer(sThreadGroup,fileUtility.getTempAudioFilePath(),fileUtility.getAudioFilepath());
        //phoneAudioRecorder = new PhoneAudioRecorder(fileUtility);
        //phoneAudioRecorder.execute();
    }

    @Override
    public void onChannelClosed(Channel channel, int i, int i2) {
        Log.d(LOG_TAG, "onChannelClosed()");
        sendMessage("Channel Closed.");
        stopRunningThreads();
        sendMessage("Thread Stopped.");
    }

    @Override
    public void onInputClosed(Channel channel, int i, int i2) {
        Log.d(LOG_TAG, "onInputClosed()");
        stopRunningThreads();
        sendMessage("Thread Stopped.");
    }

    @Override
    public void onOutputClosed(Channel channel, int i, int i2) {
        Log.d(LOG_TAG, "onOutputClosed()");
        stopRunningThreads();
        sendMessage("Thread Stopped.");
        //stopService();
    }
    //    /*Validate Google Api Client Connection*/
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
    private class InitCommunicationChannel extends TwoFactorThread{
        private final String LOCAL_TAG = LOG_TAG + "::" + InitCommunicationChannel.class.getSimpleName();

        public InitCommunicationChannel(ThreadGroup tGroup){
            super(tGroup, "InitCommunicationChannel");
        }
        @Override
        public void mainloop() {
            //connect to GoogleApiClient and find connected nodes
            if(!validateGoogleClientConnection()){
                Log.e(LOCAL_TAG,"Could not connect to Google Api Client");
                sendMessage(ServiceMessage.ERROR, Constants.ERROR,"Time-out connecting GoggleAPI");
                throw new IllegalArgumentException("Could not connect to Google Api Client");
            }
            sBTChannel.getInputStream(sApiClient).setResultCallback(
                    new ResultCallback<Channel.GetInputStreamResult>() {
                        @Override
                        public void onResult(Channel.GetInputStreamResult getInputStreamResult) {
                            if (getInputStreamResult.getStatus().isSuccess()) {
                                sBTInputStream = getInputStreamResult.getInputStream();
                                Log.d(LOCAL_TAG,"Bluetooth InputStream initialization succeed.");
                            } else {
                                Log.e(LOCAL_TAG, "Bluetooth InputStream initialization failed.");
                                //sendMessage(ServiceMessage.ERROR, Constants.ERROR,"Bluetooth InputStream initialization failed.");
                                throw new RuntimeException("Bluetooth InputStream initialization failed.");
                                // handle errors
                            }
                        }
                    });

            sBTChannel.getOutputStream(sApiClient).setResultCallback(
                    new ResultCallback<Channel.GetOutputStreamResult>() {
                        @Override
                        public void onResult(Channel.GetOutputStreamResult getOutputStreamResult) {

                            if (getOutputStreamResult.getStatus().isSuccess()) {
                                sBTOutputStream = getOutputStreamResult.getOutputStream();
                                Log.e(LOCAL_TAG, "Bluetooth OutputStream initialization succeed.");
                            }
                            else{
                                Log.d(LOCAL_TAG,"Bluetooth OutputStream initialization failed.");
                                sendMessage(ServiceMessage.ERROR, Constants.ERROR,"Bluetooth OutputStream initialization failed.");
                                throw new RuntimeException("Bluetooth OutputStream initialization failed.");


                            }
                        }
                    }
            );
        }
    }

    /*Initialize Bluetooth channel for message transmission*/
    private void initBtChannel() {
        Thread initCommunicationChannel = new InitCommunicationChannel(sThreadGroup);
        initCommunicationChannel.setUncaughtExceptionHandler(sThreadExHandler);
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
            takeRest(100);
            i++;
            if(i>50) {
                Log.e(LOG_TAG,"Bluetooth channel initialization taking more than expected time. Stopping process");
                throw new RuntimeException("Could not initialize bluetooth channel");
            }
        }
        Log.d(LOG_TAG, "Initialized bluetooth channel");

    }

    class ClientMessageHandler extends Handler{

        private  SensorValueService sService;

        public ClientMessageHandler(SensorValueService sService)
        {
            this.sService = sService;
        }

        @Override
        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
            sService.sReplyMessenger = msg.replyTo;
            ServiceMessage serMsg = ServiceMessage.getServiceMsg(msg.what);
            int msgType = msg.what;
            switch (serMsg){
                case START:
                    sService.sendMessage(ServiceMessage.START_ACK, Constants.NOTHING,"");
                    /*to show none of threads are running*/
                    sService.sendMessage(ServiceMessage.SENDER, Constants.INFO_TEXT_KEY,"STOPPED");
                    sService.sendMessage(ServiceMessage.RECEIVER, Constants.INFO_TEXT_KEY,"STOPPED");
                    sService.sendMessage(ServiceMessage.HANDLER, Constants.INFO_TEXT_KEY,"STOPPED");
                    sService.sendMessage(ServiceMessage.TIME_SYNC, Constants.INFO_TEXT_KEY,"STOPPED");
                    Log.d(LOG_TAG, "START received from main activity.");
                    break;
                case STOP:{
                    /*could invoke stop to stop service when app
                    destroyed or button pressed*/
                    break;
                }
                case START_ACK:{
                    Log.d(LOG_TAG, "BEGIN_ACK");
                    break;
                }
                case STOP_ACK:{
                    Log.d(LOG_TAG, "STOP_ACK");
                    break;
                }
                case ERROR:{
                    /*Sometime error may occur in main activity*/
                }
                case INFO:{
                    Log.e(LOG_TAG,"Unexpected Service Message.");
                    new IllegalArgumentException("Unexpected Service Message Type: "+serMsg.name());
                    break;
                }
                default: {
                    Log.e(LOG_TAG, "Wrong Service Message");
                    new IllegalArgumentException("Incorrect Message.");
                    break;
                }
            }
        }
    }

    /*
     * This service also acts as Received message handler.
     * overriding method handle() to handle received instructional message
     */
    @Override
    public void handle(IMessage msg) {
        try {
            handleMsg(msg);
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }

    /*Method that handles messages received from connected nodes*/
    public void handleMsg(IMessage msg) throws InterruptedException {
        switch (msg.getMsgType()) {
            case StartRequest.MSG_TYPE:
                Log.d(LOG_TAG, "StartRequest");
                sendMessage("START_REQUEST");
                /* Create files to store received sensor/audio values*/
                sOutboundMsgQ.add(new StartResponse(msg));
                break;
            case StopRequest.MSG_TYPE:
                Log.d(LOG_TAG,"StopRequest");
                sendMessage("STOP_REQUEST");
                sOutboundMsgQ.add(new StopResponse(msg));
                break;
            case StopResponse.MSG_TYPE:
                Log.d(LOG_TAG, "StopResponse");
                sendMessage("STOP_RESPONSE");
                break;
            case WarningMsg.MSG_TYPE:
                Log.w(LOG_TAG,"WarningMsg: ");
                sendMessage("WarningMsg");
                vibrate();
                break;
            case StartResponse.MSG_TYPE:
            case SendSensorValuesRequest.MSG_TYPE:
            case StopSensorValuesRequest.MSG_TYPE:
                Log.e(LOG_TAG, "Unexpected Message Type: " + msg.getMsgType());
                new IllegalArgumentException("Unexpected Message Type: " + msg.getMsgType());
            default:
                Log.e(LOG_TAG, "Incorrect Message");
                new IllegalArgumentException("Incorrect Message");
        }
    }


    private void sendMessage(String msg) {
        sendMessage(ServiceMessage.INFO, Constants.INFO_TEXT_KEY, msg);
    }


    private void sendMessage(ServiceMessage what){
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
