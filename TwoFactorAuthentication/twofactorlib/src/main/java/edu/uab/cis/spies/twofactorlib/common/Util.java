package edu.uab.cis.spies.twofactorlib.common;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public final class Util {

    public static void takeRest(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ie) {
            // Ignore
        }

    }
}