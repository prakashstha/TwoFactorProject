package edu.uab.cis.spies.twofactorauthentication.handler;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.uab.cis.spies.twofactorauthentication.utility.FileUtility;
import edu.uab.cis.spies.twofactorlib.messages.AudioStartTimeInfoMsg;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;

/**
 * Created by Prakashs on 9/6/15.
 */
public class AudioTimeInfoMsgHandler implements IMessageHandler {
    private static final String LOG_TAG = AudioTimeInfoMsgHandler.class.getSimpleName();
    FileUtility fileUtility = null;
    public AudioTimeInfoMsgHandler(FileUtility fileUtility){
        this.fileUtility = fileUtility;
    }
    @Override
    public void handle(IMessage msg) {
        if(!(msg instanceof AudioStartTimeInfoMsg)){
            throw new IllegalArgumentException("Incorrect msg");
        }
        AudioStartTimeInfoMsg timeInfoMsg = (AudioStartTimeInfoMsg)msg;
        Log.d(LOG_TAG, "Audio Time Info: " + timeInfoMsg.getStartTimestamp());
        BufferedWriter bfrWriter = null;
        try {
            bfrWriter = new BufferedWriter(new FileWriter(fileUtility.getAudioTimeInfoFilePath()));
            bfrWriter.write(String.valueOf(timeInfoMsg.getStartTimestamp()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            if(bfrWriter!=null)
                try {
                    bfrWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }



    }
}
