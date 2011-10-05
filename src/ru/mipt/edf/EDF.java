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

public class EDF
{
	private static EDFParser parser = null;

	public static void main(String[] args) throws IOException
	{

		parser = new EDFParser();
		File file = new File(args[0]);
		new File(file.getParent() + "/data").getAbsoluteFile().mkdir();
		InputStream is = null;
		FileOutputStream fos = null;
		InputStream format = null;
		try
		{
			is = new FileInputStream(file);
			parser.parseEDF(is);
			fos = new FileOutputStream(file.getParent() + "/" + file.getName().replaceAll("[.].*", "_header.txt"));
			format = EDFParser.class.getResourceAsStream("header.format");
			writeHeaderData(fos, getPattern(format));
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

		for (int i = 0; i < parser.getNumberOfChannels(); i++)
		{
			try
			{
				fos = new FileOutputStream(file.getParent() + "/" + file.getName().replaceAll("[.].*", "_channel_info_" + i + ".txt"));
				writeChannelData(fos, channelFormat, i);
			} finally
			{
				close(format);
				close(fos);
			}
			try
			{
				fos = new FileOutputStream(file.getParent() + "/data/" + file.getName().replaceAll("[.].*", "_" + i + ".txt"));
				for (int j = 0; j < parser.getValuesInUnits()[i].length; j++)
				{
					fos.write((parser.getValuesInUnits()[i][j] + "\n").getBytes("UTF-8"));
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

	private static void writeHeaderData(OutputStream os, String pattern) throws IOException
	{
		String message = MessageFormat.format(pattern, parser.getIdCode().trim(), parser.getSubjectID().trim(), parser.getRecordingID()
				.trim(), parser.getStartDate().trim(), parser.getStartTime().trim(), parser.getBytesInHeader(), parser.getFormatVersion()
				.trim(), parser.getNumberOfRecords(), parser.getDurationOfRecords(), parser.getNumberOfChannels());
		os.write(message.getBytes("UTF-8"));
	}

	private static void writeChannelData(OutputStream os, String pattern, int i) throws IOException
	{
		String message = MessageFormat.format(pattern, parser.getChannelLabels()[i].trim(), parser.getTransducerTypes()[i].trim(),
				parser.getDimensions()[i].trim(), parser.getMinInUnits()[i], parser.getMaxInUnits()[i], parser.getDigitalMin()[i],
				parser.getDigitalMax()[i], parser.getPrefilterings()[i].trim(), parser.getNumberOfSamples()[i],
				parser.getReserveds()[i].trim());
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
		return str.toString();
	}
}
