package spies.cis.uab.edu.tfalib.service;

/**
 * Created by Prakashs on 7/26/15.
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
