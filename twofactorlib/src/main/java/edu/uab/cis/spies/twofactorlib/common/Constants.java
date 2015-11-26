package edu.uab.cis.spies.twofactorlib.common;


/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface Constants {
//	String BT_UUID_JAVA = "a600e42069b911e4b24f0002a5d5c51b";
//	String BT_UUID_ANDROID = "a600e420-69b9-11e4-b24f-0002a5d5c51b";

    String INFO_TEXT_KEY = "I_TEXT";
    String NOTHING = "NOTHING";
    String ERROR = "ERROR";


    String START_ACTIVITY = "/start";
    String END_ACTIVITY = "/end";
    String START_ACK = "/start_ack";
    String END_ACK = "/end_ack";
    String APP_DESTROY = "/app destroy";
    String APP_STOP = "/app stop";
    String APP_RESUME = "/app resume";
    String APP_START = "/app_start";
    String MSG = "/message";

    String SENSOR_VALUE_SERVICE_NAME = "SensorValueService";

    short BYTE_SIZE = Byte.SIZE / Byte.SIZE;
    short SHORT_SIZE = Short.SIZE / Byte.SIZE;
    short INT_SIZE = Integer.SIZE / Byte.SIZE;
    short FLOAT_SZ = Float.SIZE / Byte.SIZE;
    short LONG_SIZE = Long.SIZE / Byte.SIZE;
    short DOUBLE_SZ = Double.SIZE / Byte.SIZE;

    byte ACCELROMETER = 0x01;
    byte GYROSCOPE = 0x02;
    byte AUDIO = 0x03;
    byte TEMP_AUDIO = 0x04;
    byte AUDIO_TIME_INFO = 0x05;
    byte WEAR_TIME_SYNC = 0x06;
    byte SERVER_TIME_SYNC = 0x07;
    byte PHONE_AUDIO = 0x08;
    byte TEMP_PHONE_AUDIO = 0x09;

    // Messages ids

    // RTTCalculationRequest : 0x00
    // RTTCalculationResponse : 0x01
    // ProximityMsg : 0x02
    // ManageSensorValFreqMsg : 0x03
    // WarningMsg : 0x04
    // SegmentFeaturesMsg : 0x05
    // SensorValuesStatisticsMsg : 0x06
    // EventDetailMsg: 0x07
    // ClassificationResultMsg: 0x08
    // FailedSegmentMsg: 0x09
    // HeartBeatRequest : 0x10
    // HeartBeatResponse : 0x11
    // ConfigurationMsg: 0x12
    // SegmentMsg: 0x13
    // ShortHeartBeatResponse: 0x14
    // AudioValuesMsg: 0x15
    // SendAudioValuesRequest : 0x16
    // SendAudioValuesResponse : 0x17
    // AudioStartTimeInfoMsg : 0x18
    // StartRequest : 0x20
    // StartResponse : 0x21
    // ShareTimeRequest : 0x30
    // ShareTimeResponse : 0x31
    // SensorValuesMsg : 0x40
    // StopRequest : 0x50
    // StopResponse : 0x51
    // SendSensorValuesRequest : 0x60
    // StopSensorValuesRequest : 0x70

}