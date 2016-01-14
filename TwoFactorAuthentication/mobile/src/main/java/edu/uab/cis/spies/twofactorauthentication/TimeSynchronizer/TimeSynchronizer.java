package edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer;

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class TimeSynchronizer{
    private static final String LOG_TAG = TimeSynchronizer.class
            .getSimpleName();
    private ServerTimeSynchronizer serverTimeSynchronizer= null;
    private WearTimeSynchronizer wearTimeSynchronizer= null;

    public TimeSynchronizer(ThreadGroup threadGroup,
                            LinkedBlockingQueue<IMessage> outboundMsgQ,
                            FileUtility fileUtility)
    {
        serverTimeSynchronizer = new ServerTimeSynchronizer(threadGroup, fileUtility);
        serverTimeSynchronizer.setDaemon(true);
        serverTimeSynchronizer.setPriority(Thread.NORM_PRIORITY);

        wearTimeSynchronizer = new WearTimeSynchronizer(threadGroup, outboundMsgQ, fileUtility);
        wearTimeSynchronizer.setDaemon(true);
        wearTimeSynchronizer.setPriority(Thread.NORM_PRIORITY);

    }

    public ServerTimeSynchronizer getServerTimeSynchronizer() {
        return serverTimeSynchronizer;
    }

    public WearTimeSynchronizer getWearTimeSynchronizer() {
        return wearTimeSynchronizer;
    }

    public void startTimeSync(){
       Log.d(LOG_TAG,"startTimeSync()");
       if(serverTimeSynchronizer.isAlive() || wearTimeSynchronizer.isAlive()) {
           interruptTimeSync();
       }
       serverTimeSynchronizer.start();
       wearTimeSynchronizer.start();

//        TestTimeSync testTimeSync = new TestTimeSync();
//        testTimeSync.setPriority(Thread.NORM_PRIORITY);
//        testTimeSync.setDaemon(true);
//        testTimeSync.start();


    }

    public void interruptTimeSync(){
        if(serverTimeSynchronizer.isAlive()){
            serverTimeSynchronizer.interrupt();
        }
        if(wearTimeSynchronizer.isAlive()){
            wearTimeSynchronizer.interrupt();
        }
    }

    public boolean isAlive(){
        return (getServerTimeSynchronizer().isAlive() || getWearTimeSynchronizer().isAlive());
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler handler){
        wearTimeSynchronizer.setUncaughtExceptionHandler(handler);
        serverTimeSynchronizer.setUncaughtExceptionHandler(handler);
    }

    public long getsAvgS2WTripTime() {
        Log.d(LOG_TAG, "S2WTrimpTime: " + (serverTimeSynchronizer.getsAvgM2STripTime()+wearTimeSynchronizer.getsAvgM2WTripTime()));
        return serverTimeSynchronizer.getsAvgM2STripTime()+wearTimeSynchronizer.getsAvgM2WTripTime();
    }

    //this will give you by how much server is ahead from watch
    public long getsAvgS2WTimeDiff() {
        //Log.d(LOG_TAG, "S2WTimeDiff: " + (wearTimeSynchronizer.getAvgM2WTimeDiff() - serverTimeSynchronizer.getsAvgM2STimeDiff()));
        return (serverTimeSynchronizer.getsAvgM2STimeDiff()- wearTimeSynchronizer.getAvgM2WTimeDiff());
    }

    class TestTimeSync extends Thread{
        @Override
        public void run() {
            int i = 0;
            while(i<200){
                getsAvgS2WTimeDiff();
                //getsAvgS2WTripTime();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                i++;
            }
        }
    }


}
