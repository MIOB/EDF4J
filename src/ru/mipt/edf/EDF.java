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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Scanner;

import ru.mipt.edf.EDFParser.EDFData;
import ru.mipt.edf.EDFParser.EDFHeader;
import ru.mipt.edf.EDFParser.EDFParserResult;

/**
 * This class contains a simple example program for the EDFParser.
 */
public class EDF
{
	/**
	 * This is a simple example program for the EDFParser. It will parse the file
	 * which is specified as the first argument in args.
	 * 
	 * @param args The first argument has to be to file which should be parsed
	 * @throws IOException If there is an error during parsing
	 */
	public static void main(String[] args) throws IOException
	{
		File file = new File(args[0]);
		new File(file.getParent() + "/data").getAbsoluteFile().mkdir();
		InputStream is = null;
		FileOutputStream fos = null;
		InputStream format = null;
		EDFParserResult result = null;
		try
		{
			is = new BufferedInputStream(new FileInputStream(file));
			result = EDFParser.parseEDF(is);
			fos = new FileOutputStream(file.getParent() + "/" + file.getName().replaceAll("[.].*", "_header.txt"));
			format = EDFParser.class.getResourceAsStream("header.format");
			writeHeaderData(result.getEdfHeader(), fos, getPattern(format));
		} finally
		{
			close(is);
			close(fos);
			close(format);
		}
		String channelFormat = null;
		try
		{
			format = EDFParser.class.getResourceAsStream("channel_info.format");
			channelFormat = getPattern(format);
		} finally
		{
			close(format);
		}

		for (int i = 0; i < result.getEdfHeader().getNumberOfChannels(); i++)
		{
			try
			{
				fos = new FileOutputStream(file.getParent() + "/" + file.getName().replaceAll("[.].*", "_channel_info_" + i + ".txt"));
				writeChannelData(result.getEdfHeader(), fos, channelFormat, i);
			} finally
			{
				close(format);
				close(fos);
			}
			try
			{
				EDFData data = result.getEdfData();
				fos = new FileOutputStream(file.getParent() + "/data/" + file.getName().replaceAll("[.].*", "_" + i + ".txt"));
				for (int j = 0; j < data.getValuesInUnits()[i].length; j++)
				{
					fos.write((data.getValuesInUnits()[i][j] + "\n").getBytes("UTF-8"));
				}
			} finally
			{
				close(format);
				close(fos);
			}
		}

	}

	private static void close(Closeable c)
	{
		try
		{
			c.close();
		} catch (Exception e)
		{
		}
	}

	private static void writeHeaderData(EDFHeader header, OutputStream os, String pattern) throws IOException
	{
		String message = MessageFormat.format(pattern, 
							header.getIdCode(), header.getSubjectID(), header.getRecordingID(), 
							header.getStartDate(), header.getStartTime(), header.getBytesInHeader(), 
							header.getFormatVersion(), header.getNumberOfRecords(), header.getDurationOfRecords(), 
							header.getNumberOfChannels());
		os.write(message.getBytes("UTF-8"));
	}

	private static void writeChannelData(EDFHeader header, OutputStream os, String pattern, int i) throws IOException
	{
		String message = MessageFormat.format(pattern, 
							header.getChannelLabels()[i], header.getTransducerTypes()[i],
							header.getDimensions()[i], header.getMinInUnits()[i], 
							header.getMaxInUnits()[i], header.getDigitalMin()[i],
							header.getDigitalMax()[i], header.getPrefilterings()[i], 
							header.getNumberOfSamples()[i], header.getReserveds()[i]);
		os.write(message.getBytes("UTF-8"));
	}

	private static String getPattern(InputStream is)
	{
		StringBuilder str = new StringBuilder();
		Scanner scn = new Scanner(is);
		while (scn.hasNextLine())
		{
			str.append(scn.nextLine()).append("\n");
		}
		close(scn);
		return str.toString();
	}
}
