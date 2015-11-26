package edu.uab.cis.spies.twofactorlib.common;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import static edu.uab.cis.spies.twofactorlib.common.Constants.DOUBLE_SZ;
import static edu.uab.cis.spies.twofactorlib.common.Constants.INT_SIZE;
import static edu.uab.cis.spies.twofactorlib.common.Constants.LONG_SIZE;

/**
 * <p>
 *     Utility class to convert byte stream to different data types
 *     <code>short, int, long, double, float, double[], long[], int[]</code>
 *</p>
 *
 *  Created by Prakash Shrestha on 11/26/15,
 * University of Alabama at Birmingham,
 * prakashs@uab.edu
 */
public final class ConversionUtil {

    /**
     * Convert byte stream to short
     * @param bytes
     *          byte stream
     * @return
     *          short value of byte stream
     */
    public static short conv2Short(byte[] bytes) {
        if (bytes.length != 2) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to short");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
    }
    /**
     * Convert byte stream to int
     * @param bytes
     *          byte stream
     * @return
     *          int value of byte stream
     */
    public static int conv2Int(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to integer");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }
    /**
     * Convert byte stream to long
     * @param bytes
     *          byte stream
     * @return
     *          long value of byte stream
     */
    public static long conv2Long(byte[] bytes) {
        if (bytes.length != 8) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to long");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
    }
    /**
     * Convert byte stream to double
     * @param bytes
     *          byte stream
     * @return
     *          double value of byte stream
     */
    public static double conv2Double(byte[] bytes) {
        if (bytes.length != DOUBLE_SZ) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to double");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
    }
    /**
     * Convert byte stream to double[]
     * @param bytes
     *          byte stream
     * @return
     *          double[] value of byte stream
     */
    public static double[] conv2DoubleArray(byte[] bytes) {
        int numOfElements = bytes.length / DOUBLE_SZ;
        if (bytes.length != (numOfElements * DOUBLE_SZ)) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to double[]");
        }
        double[] dArray = new double[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            dArray[i] = conv2Double(Arrays.copyOfRange(bytes, i * DOUBLE_SZ,
                    (i + 1) * DOUBLE_SZ));
        }
        return dArray;
    }
    /**
     * Convert byte stream to long[]
     * @param bytes
     *          byte stream
     * @return
     *          long[] value of byte stream
     */
    public static long[] conv2LongArray(byte[] bytes) {
        int numOfElements = bytes.length / LONG_SIZE;
        if (bytes.length != (numOfElements * LONG_SIZE)) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to long[]");
        }
        long[] lArray = new long[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            lArray[i] = conv2Long(Arrays.copyOfRange(bytes, i * LONG_SIZE,
                    (i + 1) * LONG_SIZE));
        }
        return lArray;
    }
    /**
     * Convert byte stream to int[]
     * @param bytes
     *          byte stream
     * @return
     *          int[] value of byte stream
     */
    public static int[] conv2IntArray(byte[] bytes) {
        int numOfElements = bytes.length / INT_SIZE;
        if (bytes.length != (numOfElements * INT_SIZE)) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to int[]");
        }
        int[] iArray = new int[numOfElements];
        for (int i = 0; i < numOfElements; i++) {
            iArray[i] = conv2Int(Arrays.copyOfRange(bytes, i * INT_SIZE,
                    (i + 1) * INT_SIZE));
        }
        return iArray;
    }
    /**
     * Convert byte stream to float
     * @param bytes
     *          byte stream
     * @return
     *          float value of byte stream
     */
    public static float conv2Float(byte[] bytes) {
        if (bytes.length != Constants.FLOAT_SZ) {
            throw new IllegalArgumentException(
                    "Failed to convert byte[] to float");
        }
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
    }
}