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

/**
 * This is an EDFParser which is capable of parsing files in the formats EDF and EDF+.
 * 
 * For information about EDF or EDF+ see http://www.edfplus.info/
 */
public class EDFParser
{
	private static final int IDENTIFICATION_CODE_SIZE            =  7;
	private static final int LOCAL_SUBJECT_IDENTIFICATION_SIZE   = 80;
	private static final int LOCAL_REOCRDING_IDENTIFICATION_SIZE = 80;
	private static final int START_DATE_SIZE                     =  8;
	private static final int START_TIME_SIZE                     =  8;
	private static final int HEADER_SIZE                         =  8;
	private static final int DATA_FORMAT_VERSION_SIZE            = 44;
	private static final int DURATION_DATA_RECORDS_SIZE          =  8;
	private static final int NUMBER_OF_DATA_RECORDS_SIZE         =  8;
	private static final int NUMBER_OF_CHANELS_SIZE              =  4;
	private static final int LABEL_OF_CHANNEL_SIZE               = 16;
	private static final int TRANSDUCER_TYPE_SIZE                = 80;
	private static final int PHYSICAL_DIMENSION_OF_CHANNEL_SIZE  =  8;
	private static final int PHYSICAL_MIN_IN_UNITS_SIZE          =  8;
	private static final int PHYSICAL_MAX_IN_UNITS_SIZE          =  8;
	private static final int DIGITAL_MIN_SIZE                    =  8;
	private static final int DIGITAL_MAX_SIZE                    =  8;
	private static final int PREFILTERING_SIZE                   = 80;
	private static final int NUMBER_OF_SAMPLES_SIZE              =  8;
	private static final int RESERVED_SIZE                       = 32;
	
	/**
	 * Parse the InputStream which should be at the start of an EDF-File.
	 * The method returns an object containing the complete content of the EDF-File.
	 * 
	 * @param is The InputStream to the EDF-File
	 * @return The parsed result
	 * @throws IOException If there is an error during parsing
	 */
	public static EDFParserResult parseEDF(InputStream is) throws IOException
	{
		EDFParserResult result = new EDFParserResult();
		result.edfHeader = parseEDFHeader(is);
		result.edfData   = parseEDFData(is, result.edfHeader);
		
		if (is.read() != -1)
		{
			throw new EDFParserException();
		}
		return result;
	}

	/**
	 * Parse the InputStream which should be at the start of an EDF-File.
	 * The method returns an object containing the complete header of the EDF-File
	 * 
	 * @param is The InputStream to the EDF-File
	 * @return The parsed result
	 * @throws IOException If there is an error during parsing
	 */
	public static EDFHeader parseEDFHeader(InputStream is) throws IOException
	{
		if (is.read() != '0') 
		{
			throw new EDFParserException();
		}
		EDFHeader header = new EDFHeader();
		header.idCode            = readASCIIFromStream (is, IDENTIFICATION_CODE_SIZE);
		header.subjectID         = readASCIIFromStream (is, LOCAL_SUBJECT_IDENTIFICATION_SIZE);
		header.recordingID       = readASCIIFromStream (is, LOCAL_REOCRDING_IDENTIFICATION_SIZE);
		header.startDate         = readASCIIFromStream (is, START_DATE_SIZE);
		header.startTime         = readASCIIFromStream (is, START_TIME_SIZE);
		header.bytesInHeader     = readIntFromStream   (is, HEADER_SIZE);
		header.formatVersion     = readASCIIFromStream (is, DATA_FORMAT_VERSION_SIZE);
		header.numberOfRecords   = readIntFromStream   (is, NUMBER_OF_DATA_RECORDS_SIZE);
		header.durationOfRecords = readDoubleFromStream(is, DURATION_DATA_RECORDS_SIZE);
		header.numberOfChannels  = readIntFromStream   (is, NUMBER_OF_CHANELS_SIZE);

		parseChannelInformation(is, header);
		return header;
	}

	private static void parseChannelInformation(InputStream is, EDFHeader header) throws IOException
	{
		int nc = header.numberOfChannels;
		header.channelLabels   = readBulkASCIIFromStream (is, LABEL_OF_CHANNEL_SIZE, nc);
		header.transducerTypes = readBulkASCIIFromStream (is, TRANSDUCER_TYPE_SIZE, nc);
		header.dimensions      = readBulkASCIIFromStream (is, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, nc);
		header.minInUnits      = readBulkDoubleFromStream(is, PHYSICAL_MIN_IN_UNITS_SIZE, nc);
		header.maxInUnits      = readBulkDoubleFromStream(is, PHYSICAL_MAX_IN_UNITS_SIZE, nc);
		header.digitalMin      = readBulkIntFromStream   (is, DIGITAL_MIN_SIZE, nc);
		header.digitalMax      = readBulkIntFromStream   (is, DIGITAL_MAX_SIZE, nc);
		header.prefilterings   = readBulkASCIIFromStream (is, PREFILTERING_SIZE, nc);
		header.numberOfSamples = readBulkIntFromStream   (is, NUMBER_OF_SAMPLES_SIZE, nc);
		header.reserveds       = readBulkASCIIFromStream (is, RESERVED_SIZE, nc);
	}

	private static EDFData parseEDFData(InputStream is, EDFHeader header) throws IOException
	{
		EDFData data = new EDFData();
		data.unitsInDigit = new double[header.numberOfChannels];
		for (int i = 0; i < data.unitsInDigit.length; i++)
		{
			data.unitsInDigit[i] = (header.maxInUnits[i] - header.minInUnits[i]) / (header.digitalMax[i] - header.digitalMin[i]);
		}

		data.digitalValues = new short [header.numberOfChannels][];
		data.valuesInUnits = new double[header.numberOfChannels][];
		for (int i = 0; i < header.numberOfChannels; i++)
		{
			data.digitalValues[i] = new short[header.numberOfRecords * header.numberOfSamples[i]];
			data.valuesInUnits[i] = new double[header.numberOfRecords * header.numberOfSamples[i]];
		}
		
		int samplesPerRecord = 0;
		for (int nos : header.numberOfSamples) 
		{
			samplesPerRecord += nos;
		}
		
		ReadableByteChannel ch = Channels.newChannel(is);
		ByteBuffer bytebuf = ByteBuffer.allocate(samplesPerRecord*2);
		bytebuf.order(ByteOrder.LITTLE_ENDIAN);

		for (int i = 0; i < header.numberOfRecords; i++)
		{
			bytebuf.rewind();
			ch.read(bytebuf);
			bytebuf.rewind();
			for (int j = 0; j < header.numberOfChannels; j++)
				for (int k = 0; k < header.numberOfSamples[j]; k++)
				{
					int s = header.numberOfSamples[j] * i + k;
					data.digitalValues[j][s] = bytebuf.getShort();
					data.valuesInUnits[j][s] = data.digitalValues[j][s] * data.unitsInDigit[j];
				}
		}
		return data;
	}

	private static String[] readBulkASCIIFromStream(InputStream is, int size, int length) throws IOException
	{
		String[] result = new String[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = readASCIIFromStream(is, size);
		}
		return result;
	}

	private static double[] readBulkDoubleFromStream(InputStream is, int size, int length) throws IOException
	{
		double[] result = new double[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = readDoubleFromStream(is, size);
		}
		return result;
	}

	private static int[] readBulkIntFromStream(InputStream is, int size, int length) throws IOException
	{
		int[] result = new int[length];
		for (int i = 0; i < length; i++)
		{
			result[i] = readIntFromStream(is, size);
		}
		return result;
	}
	
	private static int readIntFromStream(InputStream is, int size) throws IOException 
	{
		return Integer.parseInt(readASCIIFromStream(is, size));
	}

	private static double readDoubleFromStream(InputStream is, int size) throws IOException 
	{
		return Double.parseDouble(readASCIIFromStream(is, size));
	}

	private static String readASCIIFromStream(InputStream is, int size) throws IOException
	{
		int len = 0;
		byte[] data = new byte[size];
		len = is.read(data);
		if (len != data.length)
		{
			throw new EDFParserException();
		}
		return new String(data, "ASCII").trim();
	}
	
	/**
	 * This class represents the complete content of an EDF-File.
	 */
	public static class EDFParserResult 
	{
		private EDFHeader edfHeader;
		private EDFData   edfData;

		public EDFHeader getEdfHeader() 
		{
			return edfHeader;
		}
		public EDFData getEdfData() 
		{
			return edfData;
		}
	}
	
	/**
	 * This class represents the complete header of an EDF-File.
	 */
	public static class EDFHeader 
	{
		private String   idCode;
		private String   subjectID;
		private String   recordingID;
		private String   startDate;
		private String   startTime;
		private int      bytesInHeader;
		private String   formatVersion;
		private int      numberOfRecords;
		private double   durationOfRecords;
		private int      numberOfChannels;
		private String[] channelLabels;
		private String[] transducerTypes;
		private String[] dimensions;
		private double[] minInUnits;
		private double[] maxInUnits;
		private int[]    digitalMin;
		private int[]    digitalMax;
		private String[] prefilterings;
		private int[]    numberOfSamples;
		private String[] reserveds;
		
		public String getIdCode() 
		{
			return idCode;
		}
		public String getSubjectID() 
		{
			return subjectID;
		}
		public String getRecordingID() 
		{
			return recordingID;
		}
		public String getStartDate() 
		{
			return startDate;
		}
		public String getStartTime() 
		{
			return startTime;
		}
		public int getBytesInHeader() 
		{
			return bytesInHeader;
		}
		public String getFormatVersion() 
		{
			return formatVersion;
		}
		public int getNumberOfRecords() 
		{
			return numberOfRecords;
		}
		public double getDurationOfRecords() 
		{
			return durationOfRecords;
		}
		public int getNumberOfChannels() 
		{
			return numberOfChannels;
		}
		public String[] getChannelLabels() 
		{
			return channelLabels;
		}
		public String[] getTransducerTypes() 
		{
			return transducerTypes;
		}
		public String[] getDimensions() 
		{
			return dimensions;
		}
		public double[] getMinInUnits() 
		{
			return minInUnits;
		}
		public double[] getMaxInUnits() 
		{
			return maxInUnits;
		}
		public int[] getDigitalMin() 
		{
			return digitalMin;
		}
		public int[] getDigitalMax() 
		{
			return digitalMax;
		}
		public String[] getPrefilterings() 
		{
			return prefilterings;
		}
		public int[] getNumberOfSamples() 
		{
			return numberOfSamples;
		}
		public String[] getReserveds() 
		{
			return reserveds;
		}
	}
	
	/**
	 * This class represents the complete data records of an EDF-File.
	 */
	public static class EDFData
	{
		private double[]   unitsInDigit;
		private short [][] digitalValues;
		private double[][] valuesInUnits;
		
		public double[] getUnitsInDigit() 
		{
			return unitsInDigit;
		}
		public short[][] getDigitalValues() 
		{
			return digitalValues;
		}
		public double[][] getValuesInUnits() 
		{
			return valuesInUnits;
		}
	}
	
	/**
	 * This exception is thrown if the file format is not according to EDF.
	 */
	public static class EDFParserException extends IOException  
	{
		private static final long serialVersionUID = 1L;
		public EDFParserException() 
		{
			super("File format not according to EDF");
		}
	}
}
