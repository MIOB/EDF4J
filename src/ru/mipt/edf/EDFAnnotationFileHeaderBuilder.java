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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This builder is capable of building an EDFHeader for an EDF+ file 
 * which will contain annotations.
 * 
 * The annotations has to be available in an array according to the EDF+ specification.
 */
public class EDFAnnotationFileHeaderBuilder
{	
	private String recordingStartDate;
	private String startDate;
	private String startTime;
	private int durationOfRecord;
	private int numberOfSamples;
	
	private String patientCode = "X";
	private String patientSex = "X";
	private String patientBirthdate = "X";
	private String patientName = "X";
	private String recordingHospital = "X";
	private String recordingTechnician = "X";
	private String recordingEquipment = "X";
	
	public EDFAnnotationFileHeaderBuilder startOfRecording(Date startOfRecording)
	{
		assert startOfRecording != null;
		recordingStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(startOfRecording).toUpperCase();
		startDate = new SimpleDateFormat("dd.MM.yy").format(startOfRecording);
		startTime = new SimpleDateFormat("HH.mm.ss").format(startOfRecording);
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder durationOfRecord(int val)
	{
		assert val > 0;
		durationOfRecord = val;
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder numberOfSamples(int val)
	{
		assert val > 0;
		numberOfSamples = val;
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder patientCode(String val)
	{
		assert val != null;
		patientCode = nonSpaceString(val);
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder patientIsMale(boolean val)
	{
		patientSex = val ? "M" : "F";
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder patientBirthdate(Date birthdate)
	{
		assert birthdate != null;
		patientBirthdate = new SimpleDateFormat("dd-MMM-yyyy").format(birthdate).toUpperCase();
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder patientName(String val)
	{
		assert val != null;
		patientName = nonSpaceString(val);
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder recordingHospital(String val)
	{
		assert val != null;
		recordingHospital = nonSpaceString(val);
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder recordingTechnician(String val)
	{
		assert val != null;
		recordingTechnician = nonSpaceString(val);
		return this;
	}
	
	public EDFAnnotationFileHeaderBuilder recordingEquipment(String val)
	{
		assert val != null;
		recordingEquipment = nonSpaceString(val);
		return this;
	}
	
	private String nonSpaceString(String val)
	{
		return val.replaceAll(" ", "_");
	}
	
	public EDFHeader build()
	{
		assert recordingStartDate != null;
		assert startDate != null;
		assert startTime != null;
		assert durationOfRecord > 0;
		assert numberOfSamples > 0;
		
		EDFHeader header = new EDFHeader();
		header.idCode = String.valueOf(0);
		header.subjectID = buildPatientString();
		header.recordingID = buildRecordingString();
		header.startDate = startDate;
		header.startTime = startTime;
		header.bytesInHeader = EDFConstants.HEADER_SIZE_RECORDING_INFO + EDFConstants.HEADER_SIZE_PER_CHANNEL;
		header.formatVersion = "EDF+C";
		header.numberOfRecords = 1;
		header.durationOfRecords = durationOfRecord;
		header.numberOfChannels = 1;
		
		header.channelLabels = new String[]{"EDF Annotations"};
		header.transducerTypes = new String[]{""};
		header.dimensions = new String[]{""};
		header.minInUnits = new Double[]{0.0};
		header.maxInUnits = new Double[]{1.0};
		header.digitalMin = new Integer[]{-32768};
		header.digitalMax = new Integer[]{32767};
		header.prefilterings = new String[]{""};
		header.numberOfSamples = new Integer[]{numberOfSamples};
		header.reserveds = new byte[1][EDFConstants.RESERVED_SIZE];
		
		return header;
	}
	
	private String buildPatientString()
	{
		return new StringBuilder().append(patientCode).append(" ").append(patientSex).append(" ")
			.append(patientBirthdate).append(" ").append(patientName).toString();
	}
	
	private String buildRecordingString()
	{
		return new StringBuilder().append("Startdate").append(" ").append(recordingStartDate).append(" ")
			.append(recordingHospital).append(" ").append(recordingTechnician).append(" ")
			.append(recordingEquipment).toString();
	}
}
