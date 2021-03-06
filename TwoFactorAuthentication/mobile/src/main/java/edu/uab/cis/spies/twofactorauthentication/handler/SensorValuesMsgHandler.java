package edu.uab.cis.spies.twofactorauthentication.handler;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.uab.cis.spies.twofactorauthentication.TimeSynchronizer.TimeSynchronizer;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.bo.SensorValue;
import edu.uab.cis.spies.twofactorlib.enumerations.SensorTypes;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.SensorValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;


/**
 * 
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValuesMsgHandler implements IMessageHandler {
	private static final String LOG_TAG = SensorValuesMsgHandler.class.getSimpleName();
    private String accFilePath, gyroFilePath;
    private BufferedWriter sWriter,accWriter, gyroWriter;
    private FileUtility fileUtility;
    private boolean isInitBufferWriters = false;
    private TimeSynchronizer timeSynchronizer;

   	public SensorValuesMsgHandler(FileUtility fileUtility, TimeSynchronizer timeSynchronizer) {
		super();
        this.fileUtility = fileUtility;
        this.timeSynchronizer = timeSynchronizer;
    }

	@Override
	public void handle(IMessage msg) {


        if(!isInitBufferWriters){
            if(!fileUtility.isFileCreated()){
                fileUtility.createRecordingFiles();
            }
            isInitBufferWriters = initBufferWriter();
            Log.e(LOG_TAG, "initBufferWriter = true");
        }

		if (!(msg instanceof SensorValuesMsg)) {
			throw new IllegalArgumentException("Incorrect msg");
		}
		SensorValuesMsg sensorValuesMsg = (SensorValuesMsg) msg;
        SensorValue[] sensorVals = sensorValuesMsg.getSensorValues().asArray();

        for(SensorValue val: sensorVals){
            if(val.getSensorType() == SensorTypes.ACCELEROMETER)
            {
                if (accWriter != null) {
                    try {
                        //long syncedTime = val.getTime() + timeSynchronizer.getsAvgS2WTimeDiff();
//                        accWriter.append( timeSynchronizer.getsAvgS2WTimeDiff() + ","+val.toString()+"\n");

                        accWriter.append(System.currentTimeMillis() + ","+val.toString()+"\n");
                        accWriter.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            else
            {
                if (gyroWriter != null) {
                    try {
                        //gyroWriter.append(timeSynchronizer.getsAvgS2WTimeDiff() + "," +val.toString()+"\n");
                        gyroWriter.append(System.currentTimeMillis() + ","+val.toString()+"\n");
                        gyroWriter.flush();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		if (sWriter != null) {
			sWriter.close();
			sWriter = null;
		}
	}

    private boolean initBufferWriter(){

        Log.e(LOG_TAG, "initBufferWriter()");
        try {
            accFilePath = fileUtility.getFilePath(edu.uab.cis.spies.twofactorlib.common.Constants.ACCELROMETER);
            gyroFilePath = fileUtility.getFilePath(edu.uab.cis.spies.twofactorlib.common.Constants.GYROSCOPE);

            Log.e(LOG_TAG,"accfile:"+accFilePath + " gyro path: " + gyroFilePath);
            if(accFilePath!=null && accFilePath.length()!=0)
                accWriter = new BufferedWriter(new FileWriter(accFilePath));
            else
                throw new IllegalArgumentException("Could not create acc file");
            if(gyroFilePath!=null && gyroFilePath.length()!=0)
                gyroWriter = new BufferedWriter(new FileWriter(gyroFilePath));
            else
                throw new IllegalArgumentException("Could not create gyro file");


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
