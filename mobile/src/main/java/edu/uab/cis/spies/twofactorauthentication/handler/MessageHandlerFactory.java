

package edu.uab.cis.spies.twofactorauthentication.handler;


import edu.uab.cis.spies.twofactorlib.messages.AudioStartTimeInfoMsg;
import edu.uab.cis.spies.twofactorlib.messages.AudioValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationRequest;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationResponse;
import edu.uab.cis.spies.twofactorlib.messages.SensorValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeRequest;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeResponse;
import edu.uab.cis.spies.twofactorlib.messages.StartRequest;
import edu.uab.cis.spies.twofactorlib.messages.StartResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopRequest;
import edu.uab.cis.spies.twofactorlib.messages.StopResponse;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandlerFactory;

/**
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class MessageHandlerFactory implements IMessageHandlerFactory {

    private final IMessageHandler sAppInstructionHandler;
    private final IMessageHandler sSensorValsMsgHandler;
    private final IMessageHandler sAudioValsMsgHandler;
    private final IMessageHandler sAudioTimeInfoMsgHandler;
    private final IMessageHandler sTimeSyncMsgHandler;

    public MessageHandlerFactory(
                                 IMessageHandler appInstructionHandler,
                                 IMessageHandler sensorValsMsgHandler,
                                 IMessageHandler audioValsMsgHandler,
                                 IMessageHandler audioTimeInfoMsgHandler,
                                 IMessageHandler timeSyncMsgHandler) {
        super();
        this.sAppInstructionHandler = appInstructionHandler;
        this.sSensorValsMsgHandler = sensorValsMsgHandler;
        this.sAudioValsMsgHandler = audioValsMsgHandler;
        this.sAudioTimeInfoMsgHandler = audioTimeInfoMsgHandler;
        this.sTimeSyncMsgHandler = timeSyncMsgHandler;
    }

    @Override
    public IMessageHandler getHandler(IMessage msg) {
        switch (msg.getMsgType()) {
            case StartRequest.MSG_TYPE:
            case StopResponse.MSG_TYPE:
            case StartResponse.MSG_TYPE:
            case StopRequest.MSG_TYPE:
                return sAppInstructionHandler;
            case SensorValuesMsg.MSG_TYPE:
                return sSensorValsMsgHandler;
            case AudioValuesMsg.MSG_TYPE:
                return sAudioValsMsgHandler;
            case ShareTimeRequest.MSG_TYPE:
            case ShareTimeResponse.MSG_TYPE:
            case RTTCalculationRequest.MSG_TYPE:
            case RTTCalculationResponse.MSG_TYPE:
                return sTimeSyncMsgHandler;
            case AudioStartTimeInfoMsg.MSG_TYPE:
                return sAudioTimeInfoMsgHandler;
            default:
                throw new IllegalArgumentException("Incorrect msg");
        }
    }
}