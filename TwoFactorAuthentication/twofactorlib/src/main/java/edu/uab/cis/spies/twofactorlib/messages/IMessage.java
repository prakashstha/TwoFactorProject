package edu.uab.cis.spies.twofactorlib.messages;


import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;

/**
 * <p>
 *     Skeleton for each type of message.
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */

public interface IMessage {

    /**
     * <p>
     *     Convert the message to byte stream representation so that
     *     it can be transmitted to connected node.
     * </p>
     *
     * @return byte stream representation of message.
     */
    public byte[] toByteArray();

    /**
     * check if current byte stream indeed is a current message or not.
     */
    public void validateMsgType();

    public void handle(IMessageHandler msgHandler);

    /**
     * Each message has a unique one byte id in its header
     * that uniquely separate each message type from other type.
     * Message could be:
     *  <ul>
     *         <li>StartRequest</li>
     *         <li>StartResponse</li>
     *         <li>StopRequest</li>
     *         <li>StopResponse</li>
     *         <li>WarningMsg</li>
     *         <li>ShareTimeRequest</li>
     *         <li>ShareTimeResponse</li>
     *         <li>SendAudioValuesRequest</li>
     *         <li>SendAudioValuesResponse</li>
     *         <li>SendSensorValuesRequest</li>
     *         <li>StopSensorValuesRequest</li>
     *         <li>AudioStartTimeInfoMsg</li>
     *         <li>AudioValuesMsg</li>
     *         <li>SensorValuesMsg</li>
     *         <li>RTTCalculationRequest</li>
     *         <li>RTTCalculationResponse</li>
     *     </ul>
     * @return one byte unique id of message.
     */
    public byte getMsgType();

    /**
     *
     * @return length of message in bytes
     */
    public short getMsgLen();

    /**
     * Each message of each type create a random id to separate it's from other message of same type.
     * @return message id of a particular message.
     */
    public int getMsgId();
}
