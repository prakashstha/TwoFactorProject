package edu.uab.cis.spies.twofactorlib.threads;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorlib.common.AudioParameters;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;
import edu.uab.cis.spies.twofactorlib.common.ThreadStatus;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class AudioPlayer extends TwoFactorThread implements AudioParameters{

        private final String LOG_TAG = AudioPlayer.class.getSimpleName();
        private boolean isPlaying = false;
        private String recordingFile = null;
        private String tempAudioFilePath, audioFilePath;

    public AudioPlayer(ThreadGroup tGroup, String tempAudioFilePath, String audioFilePath){
        super(tGroup, "AudioPlayer");
        this.tempAudioFilePath = tempAudioFilePath;
        this.audioFilePath = audioFilePath;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        Log.d(LOG_TAG, ThreadStatus.INTERRUPTED);
    }

    /* Play audio */
    @Override
    protected void mainloop() throws InterruptedException {
        //findAudioRecord();
        Log.d(LOG_TAG, ThreadStatus.START);

        String inFileName = tempAudioFilePath;
        String outFileName = audioFilePath;
        copyWaveFile(inFileName, outFileName);
//           /* Get recording file to be played*/
//           if((recordingFile = getRecordingFile())!=null){
//                isPlaying = true;
//               /*
//               * audio is recorded with CHANNEL_IN_MONO configuration
//               * playing with CHANNEL_IN_STEREO configuration
//               * CHANNEL_IN_STEREO is just copy of CHANNEL_IN_MONO in two channel
//               */
//                int channel = AudioFormat.CHANNEL_IN_STEREO;
//                int bufferSize = AudioTrack.getMinBufferSize(RECORDER_SAMPLERATE,
//                        channel, RECORDER_AUDIO_ENCODING);
//                short[] audiodata = new short[bufferSize / 4];
//
//                try {
//                    DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(recordingFile)));
//                    AudioTrack audioTrack = new AudioTrack(
//                            AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE,
//                            channel, RECORDER_AUDIO_ENCODING, bufferSize,
//                            AudioTrack.MODE_STREAM);
//
//                    audioTrack.play();
//                    Log.d(LOG_TAG, ThreadStatus.RUNNING);
//                    while (isPlaying && dis.available() > 0) {
//                        int i = 0;
//                        while (dis.available() > 0 && i < audiodata.length) {
//                            audiodata[i] = dis.readShort();
//                            audiodata[i+1] = audiodata[i];
//                            i=i+2;
//                        }
//                        audioTrack.write(audiodata, 0, audiodata.length);
//                        if(isInterrupted()){
//                            break;
//                        }
//                    }
//                    dis.close();
//
//                } catch (Throwable t) {
//                    Log.e(LOG_TAG,  ThreadStatus.EXCEPTION);
//                    throw new RuntimeException(t);
//                }
//           }
//            else{
//                Log.e(LOG_TAG,ThreadStatus.EXCEPTION + "No recording file found.");
//                //throw new RuntimeException("No recording file found.");
//            }
        Log.d(LOG_TAG,ThreadStatus.END);
        }

    /*
    * Copy audio data from csv file and create audio.wav file with CHANNEL_IN_MONO configuration
    * */
    private String getRecordingFile()  {


        String recoringFile = audioFilePath;
        String csvFile = tempAudioFilePath;
        DataOutputStream dos = null;
        BufferedReader br = null;
        String line = "";
        String csvSplit = ",";
        Log.d(LOG_TAG,"Starting writing on Audio.wav file");
        Log.d(LOG_TAG,"Recording file :"+recoringFile);
        Log.d(LOG_TAG,"CSV file: " + csvFile);
        try{
            dos =  new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFilePath)));
            br = new BufferedReader(new FileReader(csvFile));
            while((line = br.readLine())!=null){
                String[] val = line.split(csvSplit);
//                for(int i = 1;i<val.length;i++){
//                    short sval = Short.parseShort(val[i]);
//                    dos.writeShort(sval);
//                }
                byte[] sval = new byte[val.length];
                for(int i = 1;i<val.length;i++){
                    sval[i] = Byte.parseByte(val[i]);
                }
                dos.write(sval);
            }
            Log.d(LOG_TAG,"Reading writing completes");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                    Log.d(LOG_TAG,"br closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dos != null) {
                try {
                    dos.close();
                    Log.d(LOG_TAG,"dos closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return recoringFile;
        }
    }

    private void copyWaveFile(String inFilename,String outFilename){
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        int intTotalAudioLen = 0;

        int totalDataLen = intTotalAudioLen + 44;
        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);

            totalAudioLen = in.getChannel().size();
            if((totalAudioLen + 44) >Integer.MAX_VALUE){
                throw new RuntimeException("file size is greate that expected");
            }
            intTotalAudioLen = (int)totalAudioLen;
            totalDataLen = intTotalAudioLen + 44;

            Log.d("CopyWaveFile()", "File size: " + totalDataLen);

            writeWaveFileHeader(out, intTotalAudioLen, totalDataLen,
                    RECORDER_SAMPLERATE, channels, RECORDER_BPP);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Sakigo ni", "HEHE");
    }

    public void writeWaveFileHeader(FileOutputStream out,
                                    int totalAudioLen,
                                    int totalDataLen,
                                    int sampleRate,
                                    short noOfChannels,
                                    short BPP) throws IOException {

        String riffHeader = "RIFF";
        String waveHeader = "WAVE";
        String fmtHeader = "fmt ";
        String data = "data";

        //short BPP = 16; //bit per sample
        //short numOfChannels = 2;//2 for stereo
        //int totalDataLen = 999999;
        //int totalAudioLen = 99966699;
        //int sampleRate =44100;

        int lengthOfFormat = 16;
        short typeOfFormat = 1; //1 for PCM
        int bytesRate = sampleRate * BPP * noOfChannels/8;
        System.out.println("Byte Rate: " + bytesRate);
        short totalBytesPerSample =  (short) ((short)(BPP * noOfChannels)/8);

        int allocSize = 44; //default header size
        /**
         * riffHeader.getBytes().length + waveHeader.getBytes().length + fmtHeader.getBytes().length + data.getBytes().length + INT_SIZE*5 + SHORT_SIZE*4;
         */
        ByteBuffer headerBuffer = ByteBuffer.allocate(allocSize);

        headerBuffer.order(ByteOrder.LITTLE_ENDIAN);

		 /* RIFF (4 bytes) */
        headerBuffer.put(riffHeader.getBytes());
		 /* File Size (4 bytes) */
        headerBuffer.putInt(totalDataLen);
		 /* WAVE (4 byte) */
        headerBuffer.put(waveHeader.getBytes());
		 /* fmt (4 bytes) */
        headerBuffer.put(fmtHeader.getBytes());
		 /* Length of format data as listed above (4 bytes) */
        headerBuffer.putInt(lengthOfFormat);
		 /* Type of format (1 for PCM) 2 bytes */
        headerBuffer.putShort(typeOfFormat);
		 /*Number of channels (2 bytes)*/
        headerBuffer.putShort(noOfChannels);
		 /*Sample Rate (4 bytes)*/
        headerBuffer.putInt(sampleRate);
		 /*number of bytes in 1 seconds (4 bytes)*/
        headerBuffer.putInt(bytesRate);
		 /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
        headerBuffer.putShort(totalBytesPerSample);
		 /*Bits per sample (2 bytes)*/
        headerBuffer.putShort(BPP);
		 /*data (4 bytes)*/
        headerBuffer.put(data.getBytes());
		 /*File size (4 bytes)*/
        headerBuffer.putInt(totalAudioLen);

        displayHeaderContent(headerBuffer);
        out.write(headerBuffer.array());

    }


    public void displayHeaderContent(ByteBuffer headerBuffer){
        byte[] arr = headerBuffer.array();
        int start, end;


        for(int i = 0;i<arr.length;i++){
            System.out.println("Header["+(i++) +"]:"+(char)arr[i]);
        }
        //RIFF (4 bytes)
        start = 0;
        end = 4; //"RIFF" is 4 byte;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(new String(Arrays.copyOfRange(arr, start, end)));

        //File Size (4 byte)
        start = end;
        end = start  + Integer.SIZE/8;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

	      /*WAVE (4 byte)*/
        start = end;
        end = start + 4; //"WAVE" is 4 byte
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(new String(Arrays.copyOfRange(arr, start, end)));

		 /* fmt (4 byte)*/
        start = end;
        end = start + 4; // "fmt " followed by null is 4 byte
        String fmt = (new String(Arrays.copyOfRange(arr, start, end)));
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(fmt + " leng: " + fmt.length());

		  /*Length of format data as listed above (4 bytes)*/
        start = end;
        end = start + Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*Type of format (1 for PCM) 2 bytes*/
        start = end;
        end = start + Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		   /*Number of channels (2 bytes)*/
        start = end;
        end = start + Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Sample Rate (4 bytes)*/
        start = end;
        end = start + Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 seconds (4 bytes)*/
        start = end;
        end = start + Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
        start = end;
        end = start + Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Bits per sample (2 bytes)*/
        start = end;
        end = start + Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*data (4 bytes)*/
        start = end;
        end = start + 4; //"data" is 4 byte
        String d = new String(Arrays.copyOfRange(arr, start, end));
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(d);

		  /*File size (4 bytes)*/
        start = end;
        end = start + Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

    }

    /*
    * Find the possible audio configuration settings on the device
    */
    public void findAudioRecord() {
        int[] mSampleRates = new int[]{8000, 11025, 22050, 44100};
        int noOfShorts = 0;

        for (int rate : mSampleRates) {
            for (short audioFormat : new short[]{AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT}) {
                for (short channelConfig : new short[]{AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO}) {
                    try {

                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);
                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {

                            short []sData = new short[bufferSize];
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {

                                Log.e(LOG_TAG, "Initialized for ::>>>>>");
                                Log.d(LOG_TAG, "Rate " + rate + "Hz, bytes: " + audioFormat + ", channel: "
                                        + channelConfig);
                                recorder.startRecording();
                                for(int i = 0;i<5;i++){
                                    noOfShorts = recorder.read(sData, 0, bufferSize);
                                    Log.d(LOG_TAG, "no of shorts read: " + noOfShorts);
                                }
                                recorder.stop();
                                recorder = null;
                            }
                           }
                    } catch (Exception e) {
                        Log.e(LOG_TAG, rate + "Exception, keep trying.", e);
                    }
                }
            }
        }
    }


}
