package spies.cis.uab.edu.tfalib.sensor.listeners;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import spies.cis.uab.edu.tfalib.bo.AudioValue;
import spies.cis.uab.edu.tfalib.common.AudioParameters;
import spies.cis.uab.edu.tfalib.common.Constants;
import spies.cis.uab.edu.tfalib.queues.AudioValuesQs;


/**
 * Created by Prakashs on 8/2/15.
 */
public class AudioRecorder extends AsyncTask<Void, Integer, Void> {
    private static final String LOG_TAG = AudioRecorder.class.getSimpleName();
    private static final int RECORDER_SAMPLERATE = AudioParameters.RECORDER_SAMPLERATE;
    private static final int RECORDER_CHANNELS = AudioParameters.RECORDER_CHANNELS;
    private static final int RECORDER_AUDIO_ENCODING = AudioParameters.RECORDER_AUDIO_ENCODING;
    private static final int bufferSize = AudioParameters.bufferSize;
    private AudioRecord recorder = null;
    private boolean isRecording = false;
    private final AudioValuesQs sAudioValsQs;
    private double sizeOfData = 0;

    public AudioRecorder(AudioValuesQs audioValuesQs){
        this.sAudioValsQs = audioValuesQs;
    }

//    int BufferElements2Rec = 1024; // want to play 2048 (2K) since 2 bytes we use only 1024
//    int BytesPerElement = 2; // 2 bytes in 16bit format


    /*
    * Method to find the format of the audio supported by the available microphone
    * */

    public void findAudioRecord() {
        int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
                                Log.e(LOG_TAG,"Initialized for ::>>>>>");
                                Log.d(LOG_TAG, "Rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                        + channelConfig);

                                //return recorder;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        //return null;
    }


    @Override
    protected Void doInBackground(Void... voids) {

       // findAudioRecord();
        short sData[] = new short[bufferSize];
        Log.v(LOG_TAG, "Starting audio capture");
        int noOfShorts = 0;
        //findAudioRecord();
        sizeOfData = 0;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING,
                bufferSize);
        int j = 0;
        long start, end, timeStamp;
        double avgLen = 0, avgTime = 0;
        //double num = 0;
        if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
            start = System.currentTimeMillis();
            recorder.startRecording();
            isRecording = true;
            Log.v(LOG_TAG, "Successfully started recording");
            while (true){
                if(isCancelled()){
                    Log.e(LOG_TAG, "Cancelled recording....");
                    break;
                }
                j++;
              //  timeStamp = System.currentTimeMillis();
                noOfShorts = recorder.read(sData, 0, bufferSize);
               // sizeOfData+= noOfShorts;
                //sAudioValsQs.add(sData, noOfShorts);

                Log.e(LOG_TAG, "No of shorts:  " + noOfShorts );
                // num+=sData.length;

            }
            end = System.currentTimeMillis();
            //Log.e(LOG_TAG, "Data Size: " + num + "\t time: "+ (end-start) + "\tRate: " + num/(end-start));



            if (recorder!=null) {
                Log.e(LOG_TAG,"stopping recording");
                isRecording = false;
                recorder.stop();
                recorder.release();
                recorder = null;
            }



        } else {
            Log.v(LOG_TAG, "Failed to started recording");
        }






        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }
}
