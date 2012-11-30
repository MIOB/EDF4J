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
import java.util.ArrayList;
import java.util.List;

import static ru.mipt.edf.ParseUtils.readASCIIFromStream;
import static ru.mipt.edf.ParseUtils.readBulkASCIIFromStream;
import static ru.mipt.edf.ParseUtils.readBulkDoubleFromStream;
import static ru.mipt.edf.ParseUtils.readBulkIntFromStream;
import static ru.mipt.edf.ParseUtils.removeElement;

/**
 * This is an EDFParser which is capable of parsing files in the formats EDF and
 * EDF+.
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

	/**
	 * Parse the InputStream which should be at the start of an EDF-File. The
	 * method returns an object containing the complete content of the EDF-File.
	 * 
	 * @param is
	 *            the InputStream to the EDF-File
	 * @return the parsed result
	 * @throws EDFParserException
	 *             if there is an error during parsing
	 */
	public static EDFParserResult parseEDF(InputStream is) throws EDFParserException
	{
		EDFParserResult result = parseHeader(is);
		parseSignal(is, result);

		return result;
	}

	/**
	 * Parse the InputStream which should be at the start of an EDF-File. The
	 * method returns an object containing the complete header of the EDF-File
	 * 
	 * @param is
	 *            the InputStream to the EDF-File
	 * @return the parsed result
	 * @throws EDFParserException
	 *             if there is an error during parsing
	 */
	public static EDFParserResult parseHeader(InputStream is) throws EDFParserException
	{
		try
		{
			if (is.read() != '0')
				throw new EDFParserException();
			EDFHeader header = new EDFHeader();
			EDFParserResult result = new EDFParserResult();
			result.header = header;

			header.idCode = readASCIIFromStream(is, IDENTIFICATION_CODE_SIZE);
			header.subjectID = readASCIIFromStream(is, LOCAL_SUBJECT_IDENTIFICATION_SIZE);
			header.recordingID = readASCIIFromStream(is, LOCAL_REOCRDING_IDENTIFICATION_SIZE);
			header.startDate = readASCIIFromStream(is, START_DATE_SIZE);
			header.startTime = readASCIIFromStream(is, START_TIME_SIZE);
			header.bytesInHeader = Integer.parseInt(readASCIIFromStream(is, HEADER_SIZE).trim());
			header.formatVersion = readASCIIFromStream(is, DATA_FORMAT_VERSION_SIZE);
			header.numberOfRecords = Integer.parseInt(readASCIIFromStream(is, NUMBER_OF_DATA_RECORDS_SIZE).trim());
			header.durationOfRecords = Double.parseDouble(readASCIIFromStream(is, DURATION_DATA_RECORDS_SIZE).trim());
			header.numberOfChannels = Integer.parseInt(readASCIIFromStream(is, NUMBER_OF_CHANELS_SIZE).trim());

			parseChannelInformation(is, result);

			return result;
		} catch (IOException e)
		{
			throw new EDFParserException(e);
		}
	}

	/**
	 * Parse only data EDF file. This method should be invoked only after
	 * parseHeader method.
	 * 
	 * @param is
	 *            stream with EDF file.
	 * @param result
	 *            results from {@link #parseHeader(is) parseHeader} method
	 * @return the parsed result
	 * @throws EDFParserException
	 *             throws if parser don't recognized EDF (EDF+) format in
	 *             stream.
	 */
	public static EDFParserResult parseSignal(InputStream is, EDFParserResult result) throws EDFParserException
	{
		try
		{
			EDFSignal signal = new EDFSignal();
			EDFHeader header = result.getHeader();

			signal.unitsInDigit = new Double[header.numberOfChannels];
			for (int i = 0; i < signal.unitsInDigit.length; i++)
				signal.unitsInDigit[i] = (header.maxInUnits[i] - header.minInUnits[i])
						/ (header.digitalMax[i] - header.digitalMin[i]);

			signal.digitalValues = new short[header.numberOfChannels][];
			signal.valuesInUnits = new double[header.numberOfChannels][];
			for (int i = 0; i < header.numberOfChannels; i++)
			{
				signal.digitalValues[i] = new short[header.numberOfRecords * header.numberOfSamples[i]];
				signal.valuesInUnits[i] = new double[header.numberOfRecords * header.numberOfSamples[i]];
			}

			int samplesPerRecord = 0;
			for (int nos : header.numberOfSamples)
			{
				samplesPerRecord += nos;
			}

			ReadableByteChannel ch = Channels.newChannel(is);
			ByteBuffer bytebuf = ByteBuffer.allocate(samplesPerRecord * 2);
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
						signal.digitalValues[j][s] = bytebuf.getShort();
						signal.valuesInUnits[j][s] = signal.digitalValues[j][s] * signal.unitsInDigit[j];
					}
			}

			result.annotations = parseAnnotation(header, signal);

			result.signal = signal;
			return result;
		} catch (IOException e)
		{
			throw new EDFParserException(e);
		}
	}

	private static List<EDFAnnotation> parseAnnotation(EDFHeader header, EDFSignal signal)
	{

		if (!header.formatVersion.startsWith("EDF+"))
			return null;

		int annotationIndex = -1;
		for (int i = 0; i < header.numberOfChannels; i++)
		{
			if ("EDF Annotations".equals(header.channelLabels[i].trim()))
			{
				annotationIndex = i;
				break;
			}
		}
		if (annotationIndex == -1)
			return null;

		short[] s = signal.digitalValues[annotationIndex];
		byte[] b = new byte[s.length * 2];
		for (int i = 0; i < s.length * 2; i += 2)
		{
			b[i] = (byte) (s[i / 2] % 256);
			b[i + 1] = (byte) (s[i / 2] / 256 % 256);
		}

		removeAnnotationSignal(header, signal, annotationIndex);

		return parseAnnotations(b);

	}

	private static List<EDFAnnotation> parseAnnotations(byte[] b)
	{
		List<EDFAnnotation> annotations = new ArrayList<EDFAnnotation>();
		int onSetIndex = 0;
		int durationIndex = -1;
		int annotationIndex = -2;
		int endIndex = -3;
		for (int i = 0; i < b.length - 1; i++)
		{
			if (b[i] == 21)
			{
				durationIndex = i;
				continue;
			}
			if (b[i] == 20 && onSetIndex > annotationIndex)
			{
				annotationIndex = i;
				continue;
			}
			if (b[i] == 20 && b[i + 1] == 0)
			{
				endIndex = i;
				continue;
			}
			if (b[i] != 0 && onSetIndex < endIndex)
			{

				String onSet = null;
				String duration = null;
				if (durationIndex > onSetIndex)
				{
					onSet = new String(b, onSetIndex, durationIndex - onSetIndex);
					duration = new String(b, durationIndex, annotationIndex - durationIndex);
				} else
				{
					onSet = new String(b, onSetIndex, annotationIndex - onSetIndex);
					duration = "";
				}
				String annotation = new String(b, annotationIndex, endIndex - annotationIndex);
				annotations.add(new EDFAnnotation(onSet, duration, annotation.split("[\u0014]")));
				onSetIndex = i;
			}
		}
		return annotations;
	}

	private static void removeAnnotationSignal(EDFHeader header, EDFSignal signal, int annotationIndex)
	{
		header.numberOfChannels--;
		removeElement(header.channelLabels, annotationIndex);
		removeElement(header.transducerTypes, annotationIndex);
		removeElement(header.dimensions, annotationIndex);
		removeElement(header.minInUnits, annotationIndex);
		removeElement(header.maxInUnits, annotationIndex);
		removeElement(header.digitalMin, annotationIndex);
		removeElement(header.digitalMax, annotationIndex);
		removeElement(header.prefilterings, annotationIndex);
		removeElement(header.numberOfSamples, annotationIndex);
		removeElement(header.reserveds, annotationIndex);

		removeElement(signal.digitalValues, annotationIndex);
		removeElement(signal.unitsInDigit, annotationIndex);
		removeElement(signal.valuesInUnits, annotationIndex);
	}

	private static void parseChannelInformation(InputStream is, EDFParserResult result) throws EDFParserException
	{
		try
		{
			EDFHeader header = result.getHeader();
			int numberOfChannels = header.numberOfChannels;
			header.channelLabels = readBulkASCIIFromStream(is, LABEL_OF_CHANNEL_SIZE, numberOfChannels);
			header.transducerTypes = readBulkASCIIFromStream(is, TRANSDUCER_TYPE_SIZE, numberOfChannels);
			header.dimensions = readBulkASCIIFromStream(is, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, numberOfChannels);
			header.minInUnits = readBulkDoubleFromStream(is, PHYSICAL_MIN_IN_UNITS_SIZE, numberOfChannels);
			header.maxInUnits = readBulkDoubleFromStream(is, PHYSICAL_MAX_IN_UNITS_SIZE, numberOfChannels);
			header.digitalMin = readBulkIntFromStream(is, DIGITAL_MIN_SIZE, numberOfChannels);
			header.digitalMax = readBulkIntFromStream(is, DIGITAL_MAX_SIZE, numberOfChannels);
			header.prefilterings = readBulkASCIIFromStream(is, PREFILTERING_SIZE, numberOfChannels);
			header.numberOfSamples = readBulkIntFromStream(is, NUMBER_OF_SAMPLES_SIZE, numberOfChannels);
			header.reserveds = new byte[numberOfChannels][];
			for (int i = 0; i < header.reserveds.length; i++)
			{
				header.reserveds[i] = new byte[RESERVED_SIZE];
				is.read(header.reserveds[i]);
			}
		} catch (IOException e)
		{
			throw new EDFParserException(e);
		}
	}

}
