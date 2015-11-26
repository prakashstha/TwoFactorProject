/*
 * Copyright 2015 (c) Secure System Group (https://se-sy.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spies.cis.uab.edu.tfalib.common;

import static spies.cis.uab.edu.tfalib.common.Constants.DOUBLE_SZ;
import static spies.cis.uab.edu.tfalib.common.Constants.INT_SIZE;
import static spies.cis.uab.edu.tfalib.common.Constants.LONG_SIZE;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * 
 * @author Swapnil Udar
 *
 */
public final class ConversionUtil {

	public static short conv2Short(byte[] bytes) {
		if (bytes.length != 2) {
			throw new IllegalArgumentException(
					"Failed to convert byte[] to short");
		}
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getShort();
	}

	public static int conv2Int(byte[] bytes) {
		if (bytes.length != 4) {
			throw new IllegalArgumentException(
					"Failed to convert byte[] to integer");
		}
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
	}

	public static long conv2Long(byte[] bytes) {
		if (bytes.length != 8) {
			throw new IllegalArgumentException(
					"Failed to convert byte[] to long");
		}
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getLong();
	}

	public static double conv2Double(byte[] bytes) {
		if (bytes.length != DOUBLE_SZ) {
			throw new IllegalArgumentException(
					"Failed to convert byte[] to double");
		}
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getDouble();
	}

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
	
	public static float conv2Float(byte[] bytes) {
		if (bytes.length != Constants.FLOAT_SZ) {
			throw new IllegalArgumentException(
					"Failed to convert byte[] to float");
		}
		return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getFloat();
	}
}