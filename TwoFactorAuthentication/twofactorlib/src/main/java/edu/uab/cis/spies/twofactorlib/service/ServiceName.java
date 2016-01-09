package edu.uab.cis.spies.twofactorlib.service;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public enum ServiceName {
    SENSOR, AUDIO;

    public static ServiceName getServiceName(int ordinal) {
        for (ServiceName name : values()) {
            if (name.ordinal() == ordinal) {
                return name;
            }
        }
        throw new IllegalArgumentException("Incorrect service messege");
    }
}
