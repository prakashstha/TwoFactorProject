package edu.uab.cis.spies.twofactorauthentication.services;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import edu.uab.cis.spies.twofactorauthentication.Constants;
import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.common.ConversionUtil;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class PhoneAudioRecorder extends AsyncTask<Void, Integer, Void> {
    private final String LOG_TAG = PhoneAudioRecorder.class.getSimpleName();
    private boolean isRecording = false;
    private String tempFile;
    private String phoneAudioFile;
    private FileUtility fileUtility;
    private int bufferSize;
    public PhoneAudioRecorder(FileUtility fileUtility){
        this.fileUtility = fileUtility;
        this.tempFile = fileUtility.getTempPhoneAudioFilePath();
        this.phoneAudioFile = fileUtility.getPhoneAudioFilePath();
       bufferSize = AudioTrack.getMinBufferSize(Constants.SAMPLERATE, Constants.AUDIO_CHANNEL_CONFIG, Constants.AUDIO_ENDCODING);
        Log.d(LOG_TAG, "Buffer size: " + bufferSize);

    }
    public boolean isRecording() {
        return isRecording;

    }

    public void setRecording(boolean isRecording) {
        this.isRecording = isRecording;
    }

    @Override
    protected Void doInBackground(Void... params) {
        isRecording = true;
        try {
            DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(new FileOutputStream(
                            new File(tempFile))));

            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, Constants.SAMPLERATE,
                    Constants.AUDIO_CHANNEL_CONFIG, Constants.AUDIO_ENDCODING, bufferSize);

            byte[] buffer = new byte[bufferSize];
            audioRecord.startRecording();
            int r = 0;
            while (isRecording) {
                int bufferReadResult = audioRecord.read(buffer, 0,
                        bufferSize);
                //Log.d(LOG_TAG,"data size: "+bufferReadResult);
//                    for (int i = 0; i < bufferReadResult; i++) {
//                        //Log.d(LOG_TAG, "data: " + buffer[i]);
//                        dos.writeShort(buffer[i]);
//                    }
                dos.write(buffer);
                publishProgress(new Integer(r));
                r++;
            }
            audioRecord.stop();
            dos.close();
        } catch (Throwable t) {
            Log.e("AudioRecord", "Recording Failed");
        }
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
        //Log.d(LOG_TAG,progress[0].toString());
    }
    protected void onPostExecute(Void result) {
        Log.d("stopRecording()", "copying to wav file...");
        copyWaveFile(tempFile,phoneAudioFile);
        Log.d("stopRecording()", "copied");
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
                    Constants.SAMPLERATE, Constants.channels, Constants.BPP);

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
       Log.d(LOG_TAG,"Finished recordings");
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

       // displayHeaderContent(headerBuffer);
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
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*Type of format (1 for PCM) 2 bytes*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		   /*Number of channels (2 bytes)*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Sample Rate (4 bytes)*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 seconds (4 bytes)*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

		  /*number of bytes in 1 sample (combining both channel) (2 bytes)*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.SHORT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Short(Arrays.copyOfRange(arr, start, end)));

		  /*Bits per sample (2 bytes)*/
        start = end;
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.SHORT_SIZE;
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
        end = start +  edu.uab.cis.spies.twofactorlib.common.Constants.INT_SIZE;
        System.out.print("["+(start+1)+","+end+"]>>");
        System.out.println(ConversionUtil.conv2Int(Arrays.copyOfRange(arr, start, end)));

    }

}
