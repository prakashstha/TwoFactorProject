package edu.uab.cis.spies.twofactorlib.threads;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.uab.cis.spies.twofactorlib.common.AudioParameters;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;
import edu.uab.cis.spies.twofactorlib.queues.AudioValuesQs;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioRecorder extends TwoFactorThread implements AudioParameters {
    private static final String LOG_TAG = AudioRecorder.class.getSimpleName();
    private final AudioValuesQs sAudioValsQs;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private boolean isTimeSet = false;
    private double sizeOfData = 0;
    private long startTimeStamp, endTimestamp;

    public AudioRecorder(ThreadGroup tGroup, AudioValuesQs audioValuesQs) {
        super(tGroup, LOG_TAG);
        this.sAudioValsQs = audioValuesQs;
        setPriority(Thread.NORM_PRIORITY);
    }


    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG, ThreadStatus.INTERRUPTED);
        if (recorder != null) {
            isRecording = false;
            recorder.stop();
            recorder.release();
//            Log.e(LOG_TAG, ThreadStatus.INTERRUPTED + "Recorder Released.");
            recorder = null;
        }
        if (sWriter != null) {
            try {
                sWriter.close();
                Log.d(LOG_TAG, ThreadStatus.INTERRUPTED + "Writer closed.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void mainloop() {
        Log.d(LOG_TAG, ThreadStatus.START);
        byte sData[] = new byte[bufferSize];
        int noOfBytes = 0;
        sizeOfData = 0;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
                bufferSize);

        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            recorder.startRecording();
            isRecording = true;
            Log.d(LOG_TAG, ThreadStatus.RUNNING);
            Log.d(LOG_TAG,"Start:" + System.currentTimeMillis());
            setStartTimeStamp(System.currentTimeMillis());
            isTimeSet = true;
            while (isRecording) {
                noOfBytes = recorder.read(sData, 0, bufferSize);
                if (noOfBytes > 0)
                    sAudioValsQs.add(sData, noOfBytes);
                if (isInterrupted()) {
                    isRecording = false;
                    break;
                }
            }
            Log.d(LOG_TAG,"End:" + System.currentTimeMillis());
            setEndTimestamp(System.currentTimeMillis());
        } else {
            Log.v(LOG_TAG, ThreadStatus.EXCEPTION + "AudioRecord Initialization failed.");
            throw new RuntimeException("AudioRecord Initialization failed.");
        }
        Log.d(LOG_TAG, ThreadStatus.END);

    }


    public boolean isTimeSet()
    {
        return isTimeSet;
    }
    private BufferedWriter sWriter;
    String fileDir, SDCardDir = null;
    File file = null;

    private void initBufferWriter() {
        //Log.d(LOG_TAG,"initBufferWriter()");
        SDCardDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        fileDir = "Sensor_values";
        file = new File(SDCardDir + File.separator + fileDir + File.separator + "debug.csv");
        // if file doesn't exists, then create it


        try {
            if (!file.exists()) {
                createDirectory(fileDir);
                file.createNewFile();

            }
            sWriter = new BufferedWriter(new FileWriter(file));

        } catch (IOException e) {
            Log.e(LOG_TAG, ThreadStatus.EXCEPTION + e.toString());
        }


    }

    private void write(String str) {

        if (sWriter != null) {
            try {
                sWriter.append(str);
                sWriter.newLine();
                sWriter.flush();
                // sWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Log.e(LOG_TAG, "sWriter null");
            initBufferWriter();
        }
    }

    private boolean createDirectory(String dirName) {

        File dirFile = new File(SDCardDir + File.separator + dirName + File.separator);
        return (dirFile.mkdirs() || dirFile.isDirectory());
    }

    public void findAudioRecord() {
        int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};
        int noOfShorts = 0;

        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {

                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

                            short[] sData = new short[bufferSize];
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {

                                Log.e(LOG_TAG, "Initialized for ::>>>>>");
                                Log.d(LOG_TAG, "Rate " + rate + "Hz, bytes: " + audioFormat + ", channel: "
                                        + channelConfig);
                                recorder.startRecording();
                                for (int i = 0; i < 5; i++) {
                                    noOfShorts = recorder.read(sData, 0, bufferSize);
                                    Log.d(LOG_TAG, "no of shorts read: " + noOfShorts);
                                }


                                //return recorder;
                            }
                            recorder.stop();

                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
        recorder.stop();
        recorder = null;
    }

    public long getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(long startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }
}
