package edu.uab.cis.spies.twofactorlib.common;

/**
 * <p>
 *     This class holds common keywords that tell the status of the thread.
 *     Thread could be in any of the following status
 *     <code>START, RUNNING, INTERRUPTED, END, EXCEPTION</code>
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface ThreadStatus {
    String START = "<-- START -->\n";
    String RUNNING = "<-- RUNNING -->\n";
    String INTERRUPTED = "<-- INTERRUPTED -->\n";
    String END = "<-- END -->\n";
    String EXCEPTION = "<-- EXCEPTION -->\n";
}
