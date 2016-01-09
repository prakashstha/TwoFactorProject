package edu.uab.cis.spies.twofactorauthentication;

import android.media.AudioFormat;

/**
 * Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public interface Constants {
    String PROPERTY_FILE_REL_PATH = "./zebra.properties";

    // File created and write to by the application
    String SENSOR_FILE_NAME = "Sensor.csv";
    String APP_DIRECTORY = "Sensor_values";
    String ACCELEROMETER_FILE_NAME = "wear_acc.csv";
    String GYROSCOPE_FILE_NAME = "wear_gyro.csv";
    String AUDIO_FILE_NAME = "wear_audio.wav";
    String AUDIO_TEMP_FILE_NAME = "wear_audio_temp.raw";
    String PHONE_AUDIO_FILE_NAME = "phone_audio.wav";
    String PHONE_AUDIO_TEMP_FILE_NAME = "phone_audio_temp.raw";
    String AUDIO_TIME_INFO_FILE_NAME = "wear_audio_time.csv";
    String WEAR_TIME_SYNC_FILE_NAME = "wear_time_sync.csv";
    String SERVER_TIME_SYNC_FILE_NAME = "server_time_sync.csv";
    String RECORDINGS_DIR = "recordings_dir";
    int SAMPLERATE = 44100;
    int AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    short channels = 2;
    short BPP = 16;
    int AUDIO_ENDCODING = AudioFormat.ENCODING_PCM_16BIT;
}
