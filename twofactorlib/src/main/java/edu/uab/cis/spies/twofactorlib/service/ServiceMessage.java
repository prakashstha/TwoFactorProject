
package edu.uab.cis.spies.twofactorlib.service;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public enum ServiceMessage {
    INFO, START,START_ACK, ERROR, STOP, STOP_ACK, STOPPED,
    ACC, GYRO, AUDIO, SENDER, RECEIVER,HANDLER,TIME_SYNC;
    // ACC, GYRO, AUDIO, SENDER, RECEIVER, HANDLER, TIME_SYCN
    // These are constants only used for service-mainactivity message exchanges
    // ;

    public static ServiceMessage getServiceMsg(int ordinal) {
        for (ServiceMessage msg : values()) {
            if (msg.ordinal() == ordinal) {
                return msg;
            }
        }
        throw new IllegalArgumentException("Incorrect service messege");
    }
}