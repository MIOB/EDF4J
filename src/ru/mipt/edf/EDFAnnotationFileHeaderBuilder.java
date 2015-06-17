/*
 * (The MIT license)
 *
 * Copyright (c) 2012 - 2015 Wolfgang Halbeisen (halbeisen.wolfgang@gmail.com) and Codemart (beniamin.oniga@codemart.ro, lia.domide@codemart.ro)
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
import java.util.Arrays;
import java.util.Date;
import static ru.mipt.edf.EDFConstants.*;

/**
 * This builder is capable of building an EDFHeader for an EDF+ file
 * which will contain annotations.
 *
 * The annotations has to be available in an array according to the EDF+ specification.
 * Changed for issue #3 from Github: https://github.com/MIOB/EDF4J/issues/3
 */
public class EDFAnnotationFileHeaderBuilder
{
        private String recordingId;
        private String recordingStartDate;
        private String startDate;
        private String startTime;
        private double durationOfRecord;
        private Integer numberOfChannels;
        private Integer numberOfRecords;

        private String patientCode = "X";
        private String patientSex = "X";
        private String patientBirthdate = "X";
        private String patientName = "X";
        private String recordingHospital = "X";
        private String recordingTechnician = "X";
        private String recordingEquipment = "X";
        private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yy");
        private final SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("HH.mm.ss");

        private String[] channelLabels;
        private String[] transducerTypes;
        private String[] dimensions;
        private Double[] minInUnits;
        private Double[] maxInUnits;
        private Integer[] digitalMin;
        private Integer[] digitalMax;
        private String[] prefilterings;
        private Integer[] numberOfSamples;
        private byte[][] reserveds;

        public EDFAnnotationFileHeaderBuilder recordingId(String recordingId)
        {
                assert recordingId != null;
                this.recordingId = nonSpaceString(recordingId);
                return this;
        }

        public EDFAnnotationFileHeaderBuilder startOfRecording(Date startOfRecording)
        {
                assert startOfRecording != null;
                recordingStartDate = new SimpleDateFormat("dd-MMM-yyyy").format(startOfRecording).toUpperCase();
                startDate = simpleDateFormat.format(startOfRecording);
                startTime = simpleTimeFormat.format(startOfRecording);
                return this;
        }

        public EDFAnnotationFileHeaderBuilder durationOfRecord(double val)
        {
                assert val > 0;
                durationOfRecord = val;
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

        public void numberOfChannels(int val)
        {
                assert val > 0;
                numberOfChannels = val;
        }

        public void numberOfRecords(int val)
        {
                assert val > 0;
                numberOfRecords = val;
        }

        public EDFAnnotationFileHeaderBuilder channelLabels(String[] channelLabels)
        {
                this.channelLabels = channelLabels;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder transducerTypes(String[] transducerTypes)
        {
                this.transducerTypes = transducerTypes;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder dimensions(String[] dimensions)
        {
                this.dimensions = dimensions;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder minInUnits(Double[] minInUnits)
        {
                this.minInUnits = minInUnits;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder maxInUnits(Double[] maxInUnits)
        {
                this.maxInUnits = maxInUnits;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder digitalMin(Integer[] digitalMin)
        {
                this.digitalMin = digitalMin;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder digitalMax(Integer[] digitalMax)
        {
                this.digitalMax = digitalMax;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder prefilterings(String[] prefilterings)
        {
                this.prefilterings = prefilterings;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder numberOfSamples(Integer[] numberOfSamples)
        {
                this.numberOfSamples = numberOfSamples;
                return this;
        }

        public EDFAnnotationFileHeaderBuilder reserveds(byte[][] reserveds)
        {
                this.reserveds = reserveds;
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

                EDFHeader header = new EDFHeader();
                header.idCode = createStringWithSpaces(String.valueOf(0), IDENTIFICATION_CODE_SIZE);
                header.subjectID = createStringWithSpaces(buildPatientString(), LOCAL_SUBJECT_IDENTIFICATION_SIZE);
                header.recordingID = recordingId != null ? recordingId : createStringWithSpaces(buildRecordingString(), LOCAL_REOCRDING_IDENTIFICATION_SIZE);

                header.startDate = startDate != null ? startDate : simpleDateFormat.format(new Date());
                header.startDate = appendSpacesToString(header.startDate, START_DATE_SIZE - header.startDate.length());

                header.startTime = startTime != null ? startTime : simpleTimeFormat.format(new Date());
                header.startTime = appendSpacesToString(header.startTime, START_TIME_SIZE - header.startTime.length());

                header.formatVersion = createStringWithSpaces("", DATA_FORMAT_VERSION_SIZE);
                header.numberOfRecords = numberOfRecords != null ? numberOfRecords : 1;
                header.durationOfRecords = durationOfRecord;
                header.numberOfChannels = numberOfChannels != null ? numberOfChannels : 1;
                header.bytesInHeader = EDFConstants.HEADER_SIZE_RECORDING_INFO +
                                       header.numberOfChannels * EDFConstants.HEADER_SIZE_PER_CHANNEL;

                header.channelLabels = channelLabels;
                header.transducerTypes = transducerTypes;
                header.dimensions = dimensions;
                header.minInUnits = minInUnits;
                header.maxInUnits = maxInUnits;
                header.digitalMin = digitalMin;
                header.digitalMax = digitalMax;
                header.prefilterings = prefilterings;
                header.numberOfSamples = numberOfSamples;
                header.reserveds = reserveds;

                return header;
        }

        private String createStringWithSpaces(String root, int totalSize) {

                return appendSpacesToString(root, totalSize - root.length());
        }

        private String appendSpacesToString(String original, int times) {

                char[] repeat = new char[times];
                Arrays.fill(repeat, ' ');
                return original + new String(repeat);
        }

        private String buildPatientString()
        {
                return patientCode + " " + patientSex + " " + patientBirthdate + " " + patientName;
        }

        private String buildRecordingString()
        {
                return "Startdate" + " " + recordingStartDate + " " + recordingHospital + " " + recordingTechnician +
                       " " + recordingEquipment;
        }
}
