package edu.uab.cis.spies.twofactorlib.common;

/**
 * <p>
 *     Message received from GCM always starts with some specific keywords.
 *     This class provides interface to holds those keywords.
 *     Messages that start with other that these keywords are discarded.
 * </p>
 *
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface GCMCommands {
    String GCM_START = "start";
    String GCM_STOP = "stop";
}
