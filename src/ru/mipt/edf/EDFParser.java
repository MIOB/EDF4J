/*
 * (The MIT license)
 * 
 * Copyright (c) 2012 MIPT (mr.santak@gmail.com)
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Collections;
import java.util.List;

/**
 * This is an EDFParser which is capable of parsing files in the formats EDF and EDF+.
 * 
 * For information about EDF or EDF+ see http://www.edfplus.info/
 */
public class EDFParser
{
	private static final int IDENTIFICATION_CODE_SIZE = 7;
	private static final int LOCAL_SUBJECT_IDENTIFICATION_SIZE = 80;
	private static final int LOCAL_REOCRDING_IDENTIFICATION_SIZE = 80;
	private static final int START_DATE_SIZE = 8;
	private static final int START_TIME_SIZE = 8;
	private static final int HEADER_SIZE = 8;
	private static final int DATA_FORMAT_VERSION_SIZE = 44;
	private static final int DURATION_DATA_RECORDS_SIZE = 8;
	private static final int NUMBER_OF_DATA_RECORDS_SIZE = 8;
	private static final int NUMBER_OF_CHANELS_SIZE = 4;
	private static final int LABEL_OF_CHANNEL_SIZE = 16;
	private static final int TRANSDUCER_TYPE_SIZE = 80;
	private static final int PHYSICAL_DIMENSION_OF_CHANNEL_SIZE = 8;
	private static final int PHYSICAL_MIN_IN_UNITS_SIZE = 8;
	private static final int PHYSICAL_MAX_IN_UNITS_SIZE = 8;
	private static final int DIGITAL_MIN_SIZE = 8;
	private static final int DIGITAL_MAX_SIZE = 8;
	private static final int PREFILTERING_SIZE = 80;
	private static final int NUMBER_OF_SAMPLES_SIZE = 8;
	private static final int RESERVED_SIZE = 32;

	private String idCode = null;
	private String subjectID = null;
	private String recordingID = null;
	private String startDate = null;
	private String startTime = null;
	private int bytesInHeader = 0;
	private String formatVersion = null;
	private int numberOfRecords = 0;
	private double durationOfRecords = 0;
	private int numberOfChannels = 0;
	private String[] channelLabels = null;
	private String[] transducerTypes = null;
	private String[] dimensions = null;
	private double[] minInUnits = null;
	private double[] maxInUnits = null;
	private int[] digitalMin = null;
	private int[] digitalMax = null;
	private String[] prefilterings = null;
	private int[] numberOfSamples = null;
	private byte[][] reserveds = null;
	private short[][] digitalValues = null;

	private double[] unitsInDigit = null;
	private double[][] valuesInUnits = null;
	private int annotationIndex = 1024;
	private List<Annotation> annotations = null;

	public EDFParser()
	{
		super();
	}

	/**
	 * Parse both data and header of EDF file.
	 * 
	 * @param is stream with EDF file.
	 * @throws IOException throws if parser don't recognized EDF (EDF+) format in stream. 
	 */
	public void parseEDF(InputStream is) throws IOException
	{
		parseHeader(is);
		parseData(is);
	}

	public final String getIdCode()
	{
		return idCode;
	}

	public final String getSubjectID()
	{
		return subjectID;
	}

	public final String getRecordingID()
	{
		return recordingID;
	}

	public final String getStartDate()
	{
		return startDate;
	}

	public final String getStartTime()
	{
		return startTime;
	}

	public final int getBytesInHeader()
	{
		return bytesInHeader;
	}

	public final String getFormatVersion()
	{
		return formatVersion;
	}

	public final int getNumberOfRecords()
	{
		return numberOfRecords;
	}

	public final double getDurationOfRecords()
	{
		return durationOfRecords;
	}

	public final int getNumberOfChannels()
	{
		return numberOfChannels;
	}

	public final String getChannelLabels(int i)
	{
		if (i >= annotationIndex)
			i++;
		return channelLabels[i];
	}

	public final String getTransducerTypes(int i)
	{
		if (i >= annotationIndex)
			i++;
		return transducerTypes[i];
	}

	public final String getDimensions(int i)
	{
		if (i >= annotationIndex)
			i++;
		return dimensions[i];
	}

	public final double getMinInUnits(int i)
	{
		if (i >= annotationIndex)
			i++;
		return minInUnits[i];
	}

	public final double getMaxInUnits(int i)
	{
		if (i >= annotationIndex)
			i++;
		return maxInUnits[i];
	}

	public final int getDigitalMin(int i)
	{
		if (i >= annotationIndex)
			i++;
		return digitalMin[i];
	}

	public final int getDigitalMax(int i)
	{
		if (i >= annotationIndex)
			i++;
		return digitalMax[i];
	}

	public final String getPrefilterings(int i)
	{
		if (i >= annotationIndex)
			i++;
		return prefilterings[i];
	}

	public final int getNumberOfSamples(int i)
	{
		if (i >= annotationIndex)
			i++;
		return numberOfSamples[i];
	}

	public final byte[] getReserveds(int i)
	{
		if (i >= annotationIndex)
			i++;
		return reserveds[i];
	}

	public final short[] getDigitalValues(int i)
	{
		if (i >= annotationIndex)
			i++;
		return digitalValues[i];
	}

	public final double[] getValuesInUnits(int i)
	{
		if (i >= annotationIndex)
			i++;
		return valuesInUnits[i];
	}

	public final List<Annotation> getAnnotations()
	{
		if (annotations == null)
			return null;
		return Collections.unmodifiableList(annotations);
	}

	private void parseAnnotation()
	{

		if (!formatVersion.startsWith("EDF+"))
			return;

		for (int i = 0; i < numberOfChannels; i++)
		{
			if ("EDF Annotations".equals(channelLabels[i].trim()))
			{
				annotationIndex = i;
				numberOfChannels--;
				break;
			}
		}
		short[] s = digitalValues[annotationIndex];
		byte[] b = new byte[s.length * 2];
		for (int i = 0; i < s.length * 2; i += 2)
		{
			b[i] = (byte) (s[i / 2] % 256);
			b[i + 1] = (byte) (s[i / 2] / 256 % 256);
		}
		annotations = Annotation.parseAnnotations(b);

	}

	/**
	 * Parse only header of EDF file.
	 * 
	 * @param is stream with EDF file.
	 * @throws IOException throws if parser don't recognized EDF (EDF+) format in stream. 
	 */
	public void parseHeader(InputStream is) throws IOException
	{
		if (is.read() != '0')
			throw new IOException("Wrong EDF format.");
		idCode = readASCIIFromStream(is, IDENTIFICATION_CODE_SIZE);
		subjectID = readASCIIFromStream(is, LOCAL_SUBJECT_IDENTIFICATION_SIZE);
		recordingID = readASCIIFromStream(is, LOCAL_REOCRDING_IDENTIFICATION_SIZE);
		startDate = readASCIIFromStream(is, START_DATE_SIZE);
		startTime = readASCIIFromStream(is, START_TIME_SIZE);
		bytesInHeader = Integer.parseInt(readASCIIFromStream(is, HEADER_SIZE).trim());
		formatVersion = readASCIIFromStream(is, DATA_FORMAT_VERSION_SIZE);
		numberOfRecords = Integer.parseInt(readASCIIFromStream(is, NUMBER_OF_DATA_RECORDS_SIZE).trim());
		durationOfRecords = Double.parseDouble(readASCIIFromStream(is, DURATION_DATA_RECORDS_SIZE).trim());
		numberOfChannels = Integer.parseInt(readASCIIFromStream(is, NUMBER_OF_CHANELS_SIZE).trim());

		parseChannelInformation(is);
	}

	private void parseChannelInformation(InputStream is) throws IOException
	{
		channelLabels = readBulkASCIIFromStream(is, LABEL_OF_CHANNEL_SIZE, numberOfChannels);
		transducerTypes = readBulkASCIIFromStream(is, TRANSDUCER_TYPE_SIZE, numberOfChannels);
		dimensions = readBulkASCIIFromStream(is, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, numberOfChannels);
		minInUnits = readBulkDoubleFromStream(is, PHYSICAL_MIN_IN_UNITS_SIZE, numberOfChannels);
		maxInUnits = readBulkDoubleFromStream(is, PHYSICAL_MAX_IN_UNITS_SIZE, numberOfChannels);
		digitalMin = readBulkIntFromStream(is, DIGITAL_MIN_SIZE, numberOfChannels);
		digitalMax = readBulkIntFromStream(is, DIGITAL_MAX_SIZE, numberOfChannels);
		prefilterings = readBulkASCIIFromStream(is, PREFILTERING_SIZE, numberOfChannels);
		numberOfSamples = readBulkIntFromStream(is, NUMBER_OF_SAMPLES_SIZE, numberOfChannels);
		reserveds = new byte[numberOfChannels][];
		for (int i = 0; i < reserveds.length; i++)
		{
			reserveds[i] = new byte[RESERVED_SIZE];
			is.read(reserveds[i]);
		}
	}

	/**
	 * Parse only data EDF file.
	 * This method should be invoked only after parseHeader method.
	 * 
	 * @param is stream with EDF file.
	 * @throws IOException throws if parser don't recognized EDF (EDF+) format in stream. 
	 */
	public void parseData(InputStream is) throws IOException
	{
		if(idCode == null)
			throw new IllegalStateException("Header have not been parsed");
		unitsInDigit = new double[numberOfChannels];
		for (int i = 0; i < unitsInDigit.length; i++)
			unitsInDigit[i] = (maxInUnits[i] - minInUnits[i]) / (digitalMax[i] - digitalMin[i]);

		digitalValues = new short[numberOfChannels][];
		valuesInUnits = new double[numberOfChannels][];
		for (int i = 0; i < numberOfChannels; i++)
		{
			digitalValues[i] = new short[numberOfRecords * numberOfSamples[i]];
			valuesInUnits[i] = new double[numberOfRecords * numberOfSamples[i]];
		}

		int samplesPerRecord = 0;
		for (int nos : numberOfSamples) 
		{
			samplesPerRecord += nos;
		}
		
		ReadableByteChannel ch = Channels.newChannel(is);
		ByteBuffer bytebuf = ByteBuffer.allocate(samplesPerRecord*2);
		bytebuf.order(ByteOrder.LITTLE_ENDIAN);

		for (int i = 0; i < numberOfRecords; i++)
		{
			bytebuf.rewind();
			ch.read(bytebuf);
			bytebuf.rewind();
			for (int j = 0; j < numberOfChannels; j++)
				for (int k = 0; k < numberOfSamples[j]; k++)
				{
					int s = numberOfSamples[j] * i + k;
					digitalValues[j][s] = bytebuf.getShort();
					valuesInUnits[j][s] = digitalValues[j][s] * unitsInDigit[j];
				}
		}
		
		parseAnnotation();
	}

	private String[] readBulkASCIIFromStream(InputStream is, int size, int length) throws IOException
	{
		String[] result = new String[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = readASCIIFromStream(is, size);
		}
		return result;
	}

	private double[] readBulkDoubleFromStream(InputStream is, int size, int length) throws IOException
	{
		double[] result = new double[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = Double.parseDouble(readASCIIFromStream(is, size).trim());
		}
		return result;
	}

	private int[] readBulkIntFromStream(InputStream is, int size, int length) throws IOException
	{
		int[] result = new int[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = Integer.parseInt(readASCIIFromStream(is, size).trim());
		}
		return result;
	}

	private String readASCIIFromStream(InputStream is, int size) throws IOException
	{
		int len = 0;
		byte[] data = new byte[size];
		len = is.read(data);
		if (len != data.length)
			throw new IOException("Wrong EDF format");
		return new String(data, "ASCII");
	}

}
