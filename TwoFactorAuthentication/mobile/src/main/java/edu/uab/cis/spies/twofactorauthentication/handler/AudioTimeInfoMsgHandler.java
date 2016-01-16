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
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
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
            bfrWriter = new BufferedWriter(new FileWriter(fileUtility.getAudioTimeInfoFilePath(), true));
            Log.d(LOG_TAG, "wear_audio_start, " + String.valueOf(timeInfoMsg.getStartTimestamp()));
            bfrWriter.write("\nwear_audio_start," + String.valueOf(timeInfoMsg.getStartTimestamp()));
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
