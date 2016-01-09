package edu.uab.cis.spies.twofactorauthentication.utility;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import edu.uab.cis.spies.twofactorauthentication.Constants;
import edu.uab.cis.spies.twofactorlib.common.AudioParameters;

/**
 *  <p>
 *      Creates necessary recording files for sensors
 *  </p>
 *  Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class FileUtility implements edu.uab.cis.spies.twofactorlib.common.Constants{

    private final String LOG_TAG = FileUtility.class.getSimpleName();
    private final String appDir = Constants.APP_DIRECTORY;
    List<String> filePathList;
    List<String> fileNameList;
    private String wearTimeSyncFilePath, serverTimeSyncFilePath;
    private byte[] type;
    private String SDCardRoot, workingDir;
    private boolean isFileCreated = false;

    public String getWorkingDir() {
        return workingDir;
    }

    public void setWorkingDir(String workingDir) {
        this.workingDir = workingDir;
    }

    public FileUtility()
    {
        filePathList = new ArrayList<String>();
        fileNameList = new ArrayList<String>();
        type = new byte[]{  ACCELROMETER,
                            GYROSCOPE,
                            AUDIO,
                            TEMP_AUDIO,
                            PHONE_AUDIO,
                            TEMP_PHONE_AUDIO,
                            AUDIO_TIME_INFO,
                            WEAR_TIME_SYNC,
                            SERVER_TIME_SYNC
                          };
        fileNameList.add(0, Constants.ACCELEROMETER_FILE_NAME);
        fileNameList.add(1, Constants.GYROSCOPE_FILE_NAME);
        fileNameList.add(2, Constants.AUDIO_FILE_NAME);
        fileNameList.add(3, Constants.AUDIO_TEMP_FILE_NAME);
        fileNameList.add(4, Constants.PHONE_AUDIO_FILE_NAME);
        fileNameList.add(5, Constants.PHONE_AUDIO_TEMP_FILE_NAME);
        fileNameList.add(6, Constants.AUDIO_TIME_INFO_FILE_NAME);
        fileNameList.add(7, Constants.WEAR_TIME_SYNC_FILE_NAME);
        fileNameList.add(8, Constants.SERVER_TIME_SYNC_FILE_NAME);
   }
    public boolean isFileCreated(){
        return isFileCreated;
    }

    public String getFilePath(byte typ) throws IOException {

        int index = findTypeIndex(typ);
        return filePathList.get(index);
     }

    public String getAudioTimeInfoFilePath(){
        int index = findTypeIndex(AUDIO_TIME_INFO);
        return filePathList.get(index);
    }
    public String getAccFilePath() {
        int index = findTypeIndex(ACCELROMETER);
        return filePathList.get(index);
    }

    public String getGyroFilePath() {
        int index = findTypeIndex(GYROSCOPE);
        return filePathList.get(index);
    }

    public String getAudioFilepath() {
        int index = findTypeIndex(AUDIO);
        return filePathList.get(index);
    }

    public String getTempAudioFilePath() {
        int index = findTypeIndex(TEMP_AUDIO);
        return filePathList.get(index);
    }

    public String getTempPhoneAudioFilePath() {
        int index = findTypeIndex(TEMP_PHONE_AUDIO);
        return filePathList.get(index);
    }

    public String getPhoneAudioFilePath() {
        int index = findTypeIndex(PHONE_AUDIO);
        return filePathList.get(index);
    }

    public String getWearTimeSyncFilePath(){
        int index = findTypeIndex(WEAR_TIME_SYNC);
        return filePathList.get(index);
    }
    public String getServerTimeSyncFilePath(){
        int index = findTypeIndex(SERVER_TIME_SYNC);
        return filePathList.get(index);

    }

    /**
     * @return true if all files are successfully created
     */
    public boolean createRecordingFiles(){
        String tempFilePath;
        if(workingDir.length() == 0){
            workingDir = getFormattedData();
        }
        SDCardRoot = getSdCardRootDirectory();

        for(byte typ: type){
            tempFilePath = getCompleteFilePath(typ);
            Log.d(LOG_TAG, tempFilePath + " creating...");
            try {
                File file = new File(tempFilePath);
                // if file doesn't exists, then create it
                if (!file.exists()) {
                    createDirectory(appDir + File.separator + workingDir);
                    file.createNewFile();
                    int index = findTypeIndex(typ);
                    filePathList.add(index, tempFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        isFileCreated = true;
        return true;
    }

    //    file handling functions
    private boolean createDirectory(String dirName)
    {
        File dirFile = new File(SDCardRoot + File.separator+ dirName + File.separator);
        return (dirFile.mkdirs() || dirFile.isDirectory());
    }
    private String getSdCardRootDirectory()
    {
        String SDCardDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        return SDCardDir;

    }
    private String getCompleteFilePath(byte type) {
        String completeFilePath = SDCardRoot + File.separator+appDir+File.separator + workingDir +File.separator+getFileName(type);
        return completeFilePath;
    }

    private String getFormattedData()
    {
        DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd-HH-mm-ss");
        Date date = new Date();
        return dateFormat.format(date);
    }
    private int findTypeIndex(byte typ){
        int index = -1;
        for(int i = 0;i<type.length;i++){
            if(typ == type[i]) {
                index = i;
                break;
            }

        }
        if(index == -1)
            throw new IllegalArgumentException("File type mismatched");
        return index;
    }
    private String getFileName(byte typ) {

        int index = findTypeIndex(typ);
        return fileNameList.get(index);
//        if(typ == ACCELROMETER)
//            return Constants.ACCELEROMETER_FILE_NAME;
//        else if(typ == GYROSCOPE)
//            return Constants.GYROSCOPE_FILE_NAME;
//        else if(typ == AUDIO)
//            return Constants.AUDIO_FILE_NAME;
//        else if(typ == TEMP_AUDIO)
//            return Constants.AUDIO_TEMP_FILE_NAME;
//        else if(typ == PHONE_AUDIO)
//            return Constants.PHONE_AUDIO_FILE_NAME;
//        else if(typ == TEMP_PHONE_AUDIO)
//            return Constants.PHONE_AUDIO_TEMP_FILE_NAME;
//        else if(typ == AUDIO_TIME_INFO)
//            return Constants.AUDIO_TIME_INFO_FILE_NAME;
//        else if(typ == WEAR_TIME_SYNC)
//            return Constants.WEAR_TIME_SYNC_FILE_NAME;
//        else if(typ == SERVER_TIME_SYNC)
//            return Constants.SERVER_TIME_SYNC_FILE_NAME;
//        else
//            throw new IllegalArgumentException(
//                    "Invalid Sensor type");
    }
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = AudioParameters.RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

    private void deleteTempFile() {
        File file = new File(Constants.AUDIO_TEMP_FILE_NAME);
        file.delete();
    }

    public void createWaveFile(){
        createWaveFileFromCSV();
//        String inFilename = getTempAudioFilePath();
//        String outFilename = getAudioFilepath();
//        FileInputStream in = null;
//        FileOutputStream out = null;
//        long totalAudioLen = 0;
//        long totalDataLen = totalAudioLen + 36;
//        long longSampleRate = AudioParameters.RECORDER_SAMPLERATE;
//        int channels = AudioParameters.channels;
//        long byteRate = AudioParameters.RECORDER_BPP * AudioParameters.RECORDER_SAMPLERATE * channels/8;
//
//        byte[] data = new byte[bufferSize];
//
//        try {
//
//            in = new FileInputStream(inFilename);
//            out = new FileOutputStream(outFilename);
//            totalAudioLen = in.getChannel().size();
//            totalDataLen = totalAudioLen + 36;
//
//            Log.d(LOG_TAG,"File size: " + totalDataLen);
//
//            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
//                    longSampleRate, channels, byteRate);
//
//            while(in.read(data) != -1){
//                out.write(data);
//            }
//
//            in.close();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void createWaveFileFromCSV(){
        String csvFile = getTempAudioFilePath();
        String audioValuesFilePath = getAudioFilepath();
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioValuesFilePath)));

            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                for(String str: data){
                    if(str.trim().length()>0)
                    {
                        short val = Short.parseShort(str);
                        dos.writeShort(val);
                    }

                }



            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Done");
    }


}
