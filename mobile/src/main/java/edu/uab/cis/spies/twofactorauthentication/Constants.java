package edu.uab.cis.spies.twofactorauthentication;

import android.media.AudioFormat;

/**
 * Created by prakashs on 7/30/2015.
 */
public interface Constants {
    String PROPERTY_FILE_REL_PATH = "./zebra.properties";

    // File created and write to by the application
    String SENSOR_FILE_NAME = "Sensor.csv";
    String APP_DIRECTORY = "Sensor_values";
    String ACCELEROMETER_FILE_NAME = "Accelerometer.csv";
    String GYROSCOPE_FILE_NAME = "Gyroscope.csv";
    String AUDIO_FILE_NAME = "audio.wav";
    String AUDIO_TEMP_FILE_NAME = "audio_temp.raw";
    String PHONE_AUDIO_FILE_NAME = "phone_audio.wav";
    String PHONE_AUDIO_TEMP_FILE_NAME = "phone_audio_temp.raw";
    String AUDIO_TIME_INFO_FILE_NAME = "audio_time_info.txt";
    String WEAR_TIME_SYNC_FILE_NAME = "wear_time_sync_data.csv";
    String SERVER_TIME_SYNC_FILE_NAME = "server_time_sync_data.csv";
    String RECORDINGS_DIR = "recordings_dir";
    int SAMPLERATE = 44100;
    int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    short channels = 2;
    short BPP = 16;
    int AUDIO_ENDCODING = AudioFormat.ENCODING_PCM_16BIT;
}
