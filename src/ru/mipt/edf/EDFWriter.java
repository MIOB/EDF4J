/*
 * (The MIT license)
 * 
 * Copyright (c) 2012 Wolfgang Halbeisen (halbeisen.wolfgang@gmail.com)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 * and associated documentation files (the "Software"), to deal in the Software without restriction, 
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, 
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies 
 * or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ru.mipt.edf;

import static ru.mipt.edf.EDFConstants.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * This class is capable of writing EDF+ data structures.
 */
public class EDFWriter 
{

	/**
	 * Writes the EDFHeader into the OutputStream.
	 * 
	 * @param header The header to write
	 * @param outputStream The OutputStream to write into
	 * @throws IOException Will be thrown if it is not possible to write into the outputStream
	 */
	public static void writeIntoOutputStream(EDFHeader header, OutputStream outputStream) throws IOException
	{
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#0.0", dfs);
		
		ByteBuffer bb = ByteBuffer.allocate(header.bytesInHeader);
		putIntoBuffer(bb, IDENTIFICATION_CODE_SIZE, header.idCode);
		putIntoBuffer(bb, LOCAL_SUBJECT_IDENTIFICATION_SIZE, header.subjectID);
		putIntoBuffer(bb, LOCAL_REOCRDING_IDENTIFICATION_SIZE, header.recordingID);
		putIntoBuffer(bb, START_DATE_SIZE, header.startDate);
		putIntoBuffer(bb, START_TIME_SIZE, header.startTime);
		putIntoBuffer(bb, HEADER_SIZE, header.bytesInHeader);
		putIntoBuffer(bb, DATA_FORMAT_VERSION_SIZE, header.formatVersion);
		putIntoBuffer(bb, NUMBER_OF_DATA_RECORDS_SIZE, header.numberOfRecords);
		putIntoBuffer(bb, DURATION_DATA_RECORDS_SIZE, header.durationOfRecords, df);
		putIntoBuffer(bb, NUMBER_OF_CHANELS_SIZE, header.numberOfChannels);
		
		putIntoBuffer(bb, LABEL_OF_CHANNEL_SIZE, header.channelLabels);
		putIntoBuffer(bb, TRANSDUCER_TYPE_SIZE, header.transducerTypes);
		putIntoBuffer(bb, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, header.dimensions);
		putIntoBuffer(bb, PHYSICAL_MIN_IN_UNITS_SIZE, header.minInUnits, df);
		putIntoBuffer(bb, PHYSICAL_MAX_IN_UNITS_SIZE, header.maxInUnits, df);
		putIntoBuffer(bb, DIGITAL_MIN_SIZE, header.digitalMin);
		putIntoBuffer(bb, DIGITAL_MAX_SIZE, header.digitalMax);
		putIntoBuffer(bb, PREFILTERING_SIZE, header.prefilterings);
		putIntoBuffer(bb, NUMBER_OF_SAMPLES_SIZE, header.numberOfSamples);
		putIntoBuffer(bb, header.reserveds);
		
		outputStream.write(bb.array());
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, Double[] values, DecimalFormat df)
	{
		for (Double value : values)
		{
			putIntoBuffer(bb, lengthPerValue, value, df);
		}
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int length, Double value, DecimalFormat df)
	{
		if (Math.floor(value) == value) {
			putIntoBuffer(bb, length, value.intValue());
		} else {
			putIntoBuffer(bb, length, df.format(value));
		}
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, Integer[] values)
	{
		for (Integer value : values)
		{
			putIntoBuffer(bb, lengthPerValue, value);
		}
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int length, int value)
	{
		putIntoBuffer(bb, length, String.valueOf(value));
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int lengthPerValue, String[] values)
	{
		for (String value : values)
		{
			putIntoBuffer(bb, lengthPerValue, value);
		}
	}
	
	private static void putIntoBuffer(ByteBuffer bb, int length, String value)
	{
		ByteBuffer valueBuffer = ByteBuffer.allocate(length);
		valueBuffer.put(value.getBytes(EDFConstants.CHARSET));
		valueBuffer.rewind();
		bb.put(valueBuffer);
	}
	
	private static void putIntoBuffer(ByteBuffer bb, byte[][] values)
	{
		for (byte[] val : values)
		{
			bb.put(val);
		}
	}
}
