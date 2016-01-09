
package edu.uab.cis.spies.twofactorauthentication.Services.message.handler;

import edu.uab.cis.spies.twofactorauthentication.Services.SensorValueService;
import edu.uab.cis.spies.twofactorlib.messages.IMessage;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationRequest;
import edu.uab.cis.spies.twofactorlib.messages.RTTCalculationResponse;
import edu.uab.cis.spies.twofactorlib.messages.SendSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.SensorValuesMsg;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeRequest;
import edu.uab.cis.spies.twofactorlib.messages.ShareTimeResponse;
import edu.uab.cis.spies.twofactorlib.messages.StartRequest;
import edu.uab.cis.spies.twofactorlib.messages.StartResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopRequest;
import edu.uab.cis.spies.twofactorlib.messages.StopResponse;
import edu.uab.cis.spies.twofactorlib.messages.StopSensorValuesRequest;
import edu.uab.cis.spies.twofactorlib.messages.WarningMsg;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandler;
import edu.uab.cis.spies.twofactorlib.messages.handler.IMessageHandlerFactory;

/**
 * Handle following MessageTypes:
 * <ul>
 *  <li><code>StartRequest</code>
 *  <li><code>StartResponse</code>
 *  <li><code>SensorValuesMsg</code>
 *  <li><code>StopRequest<code/>
 *  <li><code>StopResponse</code>
 *  <li><code>SendSensorValuesRequest</code>
 *  <li><code>StopSensorValuesRequest<code/>
 *  <li><code>ShareTimeRequest<code/>
 *  <li><code>RTTCalculationRequest<code/>
 *  <li><code>WarningMsg<code/>
 * <ul/>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public class MessageHandlerFactory implements IMessageHandlerFactory {

	private static IMessageHandler sMsgHandler;

	public MessageHandlerFactory(SensorValueService sensorValService) {
		super();
		sMsgHandler = sensorValService;
	}

	@Override
	public IMessageHandler getHandler(IMessage msg) {
		switch (msg.getMsgType()) {
		case StartRequest.MSG_TYPE:
		case StartResponse.MSG_TYPE:
		case SensorValuesMsg.MSG_TYPE:
		case StopRequest.MSG_TYPE:
		case StopResponse.MSG_TYPE:
		case SendSensorValuesRequest.MSG_TYPE:
		case StopSensorValuesRequest.MSG_TYPE:
        case ShareTimeRequest.MSG_TYPE:
        case RTTCalculationRequest.MSG_TYPE:
		case WarningMsg.MSG_TYPE:
			return sMsgHandler;
        case ShareTimeResponse.MSG_TYPE:
        case RTTCalculationResponse.MSG_TYPE:
		default:
			throw new IllegalArgumentException("Incorrect msg");
		}
	}
}