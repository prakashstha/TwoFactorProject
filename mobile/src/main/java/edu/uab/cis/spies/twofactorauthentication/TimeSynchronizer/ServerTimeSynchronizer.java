package edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import edu.uab.cis.spies.twofactorauthentication.WebServer.AsyncResponse;
import edu.uab.cis.spies.twofactorauthentication.WebServer.PushToServer1;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;
import edu.uab.cis.spies.twofactorlib.threads.TwoFactorThread;

/**
 * Created by Prakashs on 8/9/15.
 */
public class ServerTimeSynchronizer extends TwoFactorThread {

    private static final String LOG_TAG = ServerTimeSynchronizer.class
            .getSimpleName();
    private static final long TIME_SYNC_AFTER_EVERY_MS = 1000;
    private static final long TIME_SYNC_DURATION_MS = 1000;

    private PushToServer1 rttCalc, timeDiff;
    private ResponseHandler delegate;

    private static final String RTTCalc = "RTTCalc";
    private static final String TimeDiff = "TimeDifference";

    private long sTimeSyncServerMsgCounter;
    private long sAvgM2STripTime;
    private long sAvgM2STimeDiff;

    private FileUtility fileUtility;
    private String serverTimeSyncFilePath;
    private BufferedWriter bfrWriter = null;
    
    public ServerTimeSynchronizer(ThreadGroup tGroup, FileUtility fileUtility) {
        super(tGroup, LOG_TAG);
        this.fileUtility = fileUtility;
        this.sTimeSyncServerMsgCounter = 0;
        this.sAvgM2STripTime = 0;
        this.sAvgM2STimeDiff = 0;
    }

    public long getsAvgM2STripTime() {
        return sAvgM2STripTime;
    }

    public long getsAvgM2STimeDiff() {
        return sAvgM2STimeDiff;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG, "Interrupted");
    }
    @Override
    public void mainloop(){
        Log.d(LOG_TAG, ThreadStatus.RUNNING);

        try {
            serverTimeSyncFilePath = fileUtility.getServerTimeSyncFilePath();
            if (serverTimeSyncFilePath != null && serverTimeSyncFilePath.length() != 0) {
                bfrWriter = new BufferedWriter(new FileWriter(serverTimeSyncFilePath));
            } else
                throw new IllegalArgumentException("Could not create wear time sync file");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        while(!isInterrupted()){
           // long tillTime = System.currentTimeMillis() + TIME_SYNC_DURATION_MS;

            try{
//                while(!isInterrupted() && System.currentTimeMillis()<tillTime){
//                    updateTimeSyncParams();
//                    takeRest(200);
//                }
                // If loop terminated because of interruption
                if (isInterrupted()) {
                    Log.d(LOG_TAG,ThreadStatus.INTERRUPTED);
                    break;
                }
                updateTimeSyncParams();
                takeRest(TIME_SYNC_AFTER_EVERY_MS);
            }catch(InterruptedException ex){
                Log.d(LOG_TAG,ThreadStatus.INTERRUPTED + "Breaking loop: "+ ex.toString());
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




    private void updateTimeSyncParams() {

        rttCalc = new PushToServer1();
        delegate = new ResponseHandler();
        rttCalc.delegate = this.delegate;
        String requestTime = String.valueOf(System.currentTimeMillis());
        rttCalc.execute(PushToServer1.RTTCalculation,requestTime);
    }

    class ResponseHandler implements AsyncResponse{
        @Override
        public void handleResponse(String json) {
            long responseTime = System.currentTimeMillis();
            //String responseTime = String.valueOf(response_time);
            try {
                JSONObject jsonObj = new JSONObject(json);
                boolean error = jsonObj.getBoolean("error");
                // checking for error node in json
                if (!error) {
                    String reqTimeStr = jsonObj.getString("request_time");
                    String serverTimeStr = jsonObj.getString("server_time");
                    long serverTime = Long.parseLong(serverTimeStr);
                    long requestTime = Long.parseLong(reqTimeStr);
                    long oneWayTimeDelay = (responseTime - requestTime) / 2;
                    sAvgM2STimeDiff = (((responseTime - oneWayTimeDelay)-serverTime) + sAvgM2STimeDiff*sTimeSyncServerMsgCounter)/(sTimeSyncServerMsgCounter+1);
                    sTimeSyncServerMsgCounter = sTimeSyncServerMsgCounter + 1;
                    Log.d(LOG_TAG, "[Time Diff: " + getsAvgM2STimeDiff() + " ms ]");

                    /* writing all data on file */
                    String str = String.format(Locale.getDefault(), "%d,%d,%d,%d,%d", requestTime, responseTime, serverTime, oneWayTimeDelay, sAvgM2STimeDiff);
                    if(bfrWriter!=null) {
                        try {
                            bfrWriter.append(str + "\n");
                            bfrWriter.flush();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    Log.e(LOG_TAG, "Error while  timeSynchronization>> " + jsonObj.getString("message"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
