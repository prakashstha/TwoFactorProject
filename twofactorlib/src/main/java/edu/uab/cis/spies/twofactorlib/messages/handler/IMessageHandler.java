package edu.uab.cis.spies.twofactorlib.messages.handler;

import edu.uab.cis.spies.twofactorlib.messages.IMessage;

/**
 * <p>
 *     Interface to handle a message from connected node.
 *     Different messages are handled different.
 *     Message could be:
 *     <ul>
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
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface IMessageHandler {

    public void handle(IMessage msg);
}