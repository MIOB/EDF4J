package ru.mipt.edf;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Scanner;

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
	private String[] reserveds = null;
	private short[][] digitalValues = null;

	private double[] unitsInDigit = null;
	private double[][] valuesInUnits = null;

	public EDFParser()
	{
		super();
	}

	public void parseEDF(InputStream is) throws IOException
	{
		parseHeader(is);
		parseData(is);

		if (is.read() != -1)
			throw new IOException("Не верный формат EDF файла");
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

	public final String[] getChannelLabels()
	{
		return channelLabels;
	}

	public final String[] getTransducerTypes()
	{
		return transducerTypes;
	}

	public final String[] getDimensions()
	{
		return dimensions;
	}

	public final double[] getMinInUnits()
	{
		return minInUnits;
	}

	public final double[] getMaxInUnits()
	{
		return maxInUnits;
	}

	public final int[] getDigitalMin()
	{
		return digitalMin;
	}

	public final int[] getDigitalMax()
	{
		return digitalMax;
	}

	public final String[] getPrefilterings()
	{
		return prefilterings;
	}

	public final int[] getNumberOfSamples()
	{
		return numberOfSamples;
	}

	public final String[] getReserveds()
	{
		return reserveds;
	}

	public final short[][] getDigitalValues()
	{
		return digitalValues;
	}

	public final double[][] getValuesInUnits()
	{
		return valuesInUnits;
	}

	private void parseHeader(InputStream is) throws IOException
	{
		if (is.read() != '0')
			throw new IOException("Не верный формат EDF файла");
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
		reserveds = readBulkASCIIFromStream(is, RESERVED_SIZE, numberOfChannels);
	}

	private void parseData(InputStream is) throws IOException
	{
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

		for (int i = 0; i < numberOfRecords; i++)
			for (int j = 0; j < numberOfChannels; j++)
				for (int k = 0; k < numberOfSamples[j]; k++)
				{
					int s = numberOfSamples[j] * i + k;
					digitalValues[j][s] = readShortFromStream(is);
					valuesInUnits[j][s] = digitalValues[j][s] * unitsInDigit[j];
				}
	}

	private short readShortFromStream(InputStream is) throws IOException
	{
		int len = 0;
		byte[] data = new byte[2];
		len = is.read(data);
		if (len != data.length)
			throw new IOException("Не верный формат EDF файла");
		return (short) (data[0] | data[1] * 256);
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
			throw new IOException("Не верный формат EDF файла");
		return new String(data, "ASCII");
	}

}
