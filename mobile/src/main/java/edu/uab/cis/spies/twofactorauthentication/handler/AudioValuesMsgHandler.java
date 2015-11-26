package edu.uab.cis.spies.twofactorauthentication.handler;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.bo.AudioValue;
import edu.uab.cis.spies.twofactorlib.bo.AudioValues;
import edu.uab.cis.spies.twofactorlib.common.Constants;
import edu.uab.cis.spies.twofactorlib.messages.AudioValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;

/**
 * Created by Prakashs on 8/1/15.
 */
public class AudioValuesMsgHandler implements IMessageHandler{
    private static final String LOG_TAG = AudioValuesMsgHandler.class.getSimpleName();

    private DataOutputStream dos;
    private String audioValuesFilePath;
    private FileUtility fileUtility;
    private boolean isInitWriter = false;
    private List<AudioValue> audioValueList = null;
    AudioValuesMsg audioValuesMsg = null;
    AudioValues audioMsgs = null;
    public AudioValuesMsgHandler(FileUtility fileUtility)
    {
        super();
        this.fileUtility = fileUtility;
        audioValueList = new ArrayList<AudioValue>();

    }


    @Override
    public void handle(IMessage msg) {
        if(!isInitWriter){
            if(!fileUtility.isFileCreated()){
                fileUtility.createRecordingFiles();
            }
            isInitWriter = initDataOutputStream();
            Log.d(LOG_TAG,"Audio value writer initiated");
        }

        if(!(msg instanceof AudioValuesMsg)){
            throw new IllegalArgumentException("Incorrect msg");
        }
        audioValuesMsg = (AudioValuesMsg) msg;

        if(dos!=null)
        {
            try{
                audioMsgs =  audioValuesMsg.getsAudioValues();
                audioValueList.clear();
                audioValueList.addAll(audioMsgs.asList());
                for(AudioValue val: audioValueList){
                    dos.write(val.getValue());
                }
            }catch (IOException e){
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.e(LOG_TAG, "in finalize()");
        if(dos!=null)
            dos.close();
    }

    private boolean initDataOutputStream(){
        try{
            audioValuesFilePath = fileUtility.getFilePath(Constants.TEMP_AUDIO);
            if(audioValuesFilePath!=null && audioValuesFilePath.length()!=0) {
                dos = new DataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(
                                new File(audioValuesFilePath))));

            }
            else
                throw new IllegalArgumentException("Could not create audio file");
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
        return true;
    }

}
