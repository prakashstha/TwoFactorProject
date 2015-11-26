package edu.uab.cis.spies.twofactorlib.messages.handler;


import edu.uab.cis.spies.twofactorlib.messages.IMessage;

/**
 * <p>
 *     Factory of handlers that handle the messages
 * </p>
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface IMessageHandlerFactory {
    public IMessageHandler getHandler(IMessage msg);
}