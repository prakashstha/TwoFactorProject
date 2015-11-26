package edu.uab.cis.spies.twofactorlib.sensor;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingQueue;

import edu.uab.cis.spies.twofactorlib.bo.SensorValue;
import edu.uab.cis.spies.twofactorlib.common.Constants;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class SensorValueWriter extends Thread {
    private static final String LOG_TAG = SensorValueWriter.class.getSimpleName();
    private String filePath;
    private SensorTypes sensorType;
    private String SDCardRoot;
    private String appDir = "Zebra";
    private LinkedBlockingQueue<SensorValue> sSensorValueQs;

    public SensorValueWriter(LinkedBlockingQueue<SensorValue> sSensorValueQs, SensorTypes sensorType) {
        this.sSensorValueQs = sSensorValueQs;
        SDCardRoot = getSdCardRootDirectory();
        this.sensorType = sensorType;
        if (sensorType == SensorTypes.ACCELEROMETER)
            filePath = getCompleteFilePath(Constants.ACCELROMETER);
        else if (sensorType == SensorTypes.GYROSCOPE)
            filePath = getCompleteFilePath(Constants.GYROSCOPE);
        setDaemon(true);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String getFileName(byte type) {
        String filename = "";
        if (type == Constants.ACCELROMETER)
            filename = "Accelerometer.csv";
        else if (type == Constants.GYROSCOPE)
            filename = "Gyroscope.csv";
        return filename;
    }

    @Override
    public void run() {
        try {
            Log.d(LOG_TAG, "Started");
            write();
            Log.d(LOG_TAG, "Finished");
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error while writing input events to a file : "
                    + ioe.getLocalizedMessage());
            throw new RuntimeException(ioe);
        }
    }

    public boolean createDirectory(String dirName) {
        File dirFile = new File(SDCardRoot + File.separator + dirName + File.separator);
        return (dirFile.mkdirs() || dirFile.isDirectory());
    }

    private void write() throws IOException {
        FileWriter fileWriter = null;
        BufferedWriter buffWriter = null;

        File file = new File(filePath);

        // if file doesn't exists, then create it
        if (!file.exists()) {
            createDirectory(appDir);
            file.createNewFile();
        }

        try {
            fileWriter = new FileWriter(file);
            buffWriter = new BufferedWriter(fileWriter);
            Log.d(LOG_TAG, "Started writing sensor readings to file: " + filePath);


            while (!isInterrupted()) {
                try {
                    if (sSensorValueQs.isEmpty()) {
                        takeRest(10);
                        continue;
                    }
                    SensorValue sensorValue = sSensorValueQs.remove();
                    //Log.d(LOG_TAG, "Writing in " + sensorType + "\n" + sensorValue.toString());
                    buffWriter.append(sensorValue.toString());
                    buffWriter.newLine();
                    buffWriter.flush();
                } catch (NoSuchElementException nse) {
                    // All elements in the queue are processed. Wait for new
                    // elements to be added.
                    takeRest(100);
                    continue;
                }
            }
            if (!sSensorValueQs.isEmpty()) {
                while (!sSensorValueQs.isEmpty()) {
                    SensorValue sensorValue = sSensorValueQs.remove();
                    Log.d(LOG_TAG, "Writing in " + sensorType + "\n" + sensorValue.toString());
                    buffWriter.append(sensorValue.toString());
                    buffWriter.newLine();
                    buffWriter.flush();
                }
            }
        } finally {
            if (buffWriter != null) {
                buffWriter.close();
                buffWriter = null;
            }
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
            }
        }
    }

    private void takeRest(long i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String getSdCardRootDirectory() {
        String SDCardDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        return SDCardDir;

    }

    public String getCompleteFilePath(byte type) {
        String completeFilePath = SDCardRoot + File.separator + appDir + File.separator + getFileName(type);
        return completeFilePath;
    }
}
