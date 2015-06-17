/*
 * (The MIT license)
 *
 * Copyright (c) 2012 - 2015 MIPT (mr.santak@gmail.com) and Codemart (beniamin.oniga@codemart.ro, lia.domide@codemart.ro)
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.*;
import java.net.URL;
import java.nio.BufferOverflowException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EDFWriterTest {

        private static final String EDF_INPUT_FILE = "test_generator.edf";
        private static final String EDF_OUTPUT_FILE = "outputEdf.edf";
        private static final String INVALID_EDF = "invalid.edf";

        @Before
        @After
        public void cleanup() {

                try {
                        Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                        Files.delete(outputFilePath);

                } catch (Exception ex) {
                        // ignore, as not being relevant.
                }
        }

        @Test
        public void writeAndReadHeaderShouldReturnTheSameHeader() throws IOException {

                EDFHeader header = buildHeader();

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                EDFWriter.writeIntoOutputStream(header, out);

                EDFHeader parsedHeader = EDFParser.parseHeader(new ByteArrayInputStream(out.toByteArray())).getHeader();
                assertHeader(header, parsedHeader);
        }

        @Test
        public void testEdfWriter() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                // read and parse a real edf file
                URL resource = getClass().getClassLoader().getResource(EDF_INPUT_FILE);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParserResult result = EDFParser.parseEDF(is);
                EDFHeader header = result.getHeader();
                EDFSignal signal = result.getSignal();

                // write header and data to a new output file
                FileOutputStream fos = new FileOutputStream(outputFilePath.toFile());
                EDFWriter.writeIntoOutputStream(header, fos);
                EDFWriter.writeIntoOutputStream(signal, header, fos);
                fos.close();

                // read and parse the edf file created previously and compare with the header and data
                // of the initial edf file
                is = new FileInputStream(outputFilePath.toFile());
                result = EDFParser.parseEDF(is);
                is.close();
                assertHeader(header, result.getHeader());
                assertSignal(signal, result.getSignal());

        }

        @Test(expected = BufferOverflowException.class)
        public void testWriteLessBytesInHeader() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                // read and parse a real edf file
                URL resource = getClass().getClassLoader().getResource(EDF_INPUT_FILE);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParserResult result = EDFParser.parseEDF(is);
                EDFHeader header = result.getHeader();
                header.bytesInHeader--;

                try (FileOutputStream fos = new FileOutputStream(outputFilePath.toFile())) {
                        EDFWriter.writeIntoOutputStream(header, fos);
                }
        }

        @Test
        public void testWriteMoreBytesInHeader() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                // read and parse a real edf file
                URL resource = getClass().getClassLoader().getResource(EDF_INPUT_FILE);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParserResult result = EDFParser.parseEDF(is);
                EDFHeader header = result.getHeader();
                header.bytesInHeader *= 2;

                try (FileOutputStream fos = new FileOutputStream(outputFilePath.toFile())) {
                        EDFWriter.writeIntoOutputStream(header, fos);
                }
        }

        @Test(expected = BufferOverflowException.class)
        public void testExceedIdCodeSize() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                EDFHeader header = buildHeader();
                header.idCode = "         ";
                assertTrue(header.idCode.length() > EDFConstants.IDENTIFICATION_CODE_SIZE);

                try (FileOutputStream fos = new FileOutputStream(outputFilePath.toFile())) {
                        EDFWriter.writeIntoOutputStream(header, fos);
                }
        }

        @Test(expected = BufferOverflowException.class)
        public void testWriteArrayMoreElements() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                EDFHeader header = buildHeader();
                header.channelLabels = new String[] { "ch1", "ch2" };

                try (FileOutputStream fos = new FileOutputStream(outputFilePath.toFile())) {
                        EDFWriter.writeIntoOutputStream(header, fos);
                }
        }

        @Test(expected = NumberFormatException.class)
        public void testWriteArrayLessElements() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                // read and parse a real edf file
                URL resource = getClass().getClassLoader().getResource(EDF_INPUT_FILE);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParserResult result = EDFParser.parseEDF(is);
                EDFHeader header = result.getHeader();
                EDFSignal signal = result.getSignal();

                // remove two elements
                List<String> channelsList = new ArrayList<>(Arrays.asList(header.channelLabels));
                channelsList.remove(0);
                channelsList.remove(1);
                header.channelLabels = channelsList.toArray(new String[channelsList.size()]);

                // write header and data to a new output file
                FileOutputStream fos = new FileOutputStream(outputFilePath.toFile());
                EDFWriter.writeIntoOutputStream(header, fos);
                EDFWriter.writeIntoOutputStream(signal, header, fos);
                fos.close();

                // read and parse the edf file created previously and compare with the header and data
                // of the initial edf file
                try (FileInputStream is2 = new FileInputStream(outputFilePath.toFile())){
                        EDFParser.parseEDF(is2);
                }
        }

        @Test
        public void testWriteDurationOfRecord() throws Exception {

                Path outputFilePath = Paths.get(EDF_OUTPUT_FILE);
                Files.createFile(outputFilePath);

                // read and parse a real edf file
                URL resource = getClass().getClassLoader().getResource(EDF_INPUT_FILE);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParserResult result = EDFParser.parseEDF(is);
                EDFHeader header = result.getHeader();
                EDFSignal signal = result.getSignal();

                // set value for duration of a record
                header.durationOfRecords = 0.00006;

                // write header and data to a new output file
                FileOutputStream fos = new FileOutputStream(outputFilePath.toFile());
                EDFWriter.writeIntoOutputStream(header, fos);
                EDFWriter.writeIntoOutputStream(signal, header, fos);
                fos.close();

                // read and parse the edf file created previously and compare with the header and data
                // of the initial edf file
                try (FileInputStream is2 = new FileInputStream(outputFilePath.toFile())){
                        result = EDFParser.parseEDF(is2);
                        assertEquals(header.durationOfRecords, result.header.durationOfRecords, 0);
                }
        }

        @Test(expected = EDFParserException.class)
        public void testParseInvalidEdf() throws Exception {

                // read and parse an invalid edf file
                URL resource = getClass().getClassLoader().getResource(INVALID_EDF);
                assert resource != null;
                Path edfInputFile = Paths.get(resource.toURI());
                FileInputStream is = new FileInputStream(edfInputFile.toFile());
                EDFParser.parseEDF(is);
        }

        private void assertHeader(EDFHeader expected, EDFHeader actual) {

                assertEquals(expected.getIdCode(), actual.getIdCode());
                assertEquals(expected.getSubjectID(), actual.getSubjectID());
                assertEquals(expected.getRecordingID(), actual.getRecordingID());
                assertEquals(expected.getStartDate(), actual.getStartDate());
                assertEquals(expected.getStartTime(), actual.getStartTime());
                assertEquals(expected.getBytesInHeader(), actual.getBytesInHeader());
                assertEquals(expected.getFormatVersion(), actual.getFormatVersion());
                assertEquals(expected.getNumberOfRecords(), actual.getNumberOfRecords());
                assertEquals(0, Double.compare(expected.getDurationOfRecords(), actual.getDurationOfRecords()));
                assertEquals(expected.getNumberOfChannels(), actual.getNumberOfChannels());

                for (int i = 0; i < expected.getNumberOfChannels(); i++) {
                        assertEquals(expected.getChannelLabels()[i].trim(), actual.getChannelLabels()[i].trim());
                        assertEquals(expected.getTransducerTypes()[i].trim(), actual.getTransducerTypes()[i].trim());
                        assertEquals(expected.getDimensions()[i].trim(), actual.getDimensions()[i].trim());
                        assertEquals(0, Double.compare(expected.getMinInUnits()[i], actual.getMinInUnits()[i]));
                        assertEquals(0, Double.compare(expected.getMaxInUnits()[i], actual.getMaxInUnits()[i]));
                        assertEquals(expected.getDigitalMin()[i], actual.getDigitalMin()[i]);
                        assertEquals(expected.getDigitalMax()[i], actual.getDigitalMax()[i]);
                        assertEquals(expected.getPrefilterings()[i].trim(), actual.getPrefilterings()[i].trim());
                        assertEquals(expected.getNumberOfSamples()[i], actual.getNumberOfSamples()[i]);
                        assertArrayEquals(expected.getReserveds()[i], actual.getReserveds()[i]);
                }
        }

        private void assertSignal(EDFSignal expected, EDFSignal actual) {

                short[][] expectedDigitalValues = expected.getDigitalValues();
                short[][] actualDigitalValues = actual.getDigitalValues();
                assertEquals(expectedDigitalValues.length, actualDigitalValues.length);

                for (int i = 0; i < expectedDigitalValues.length; i++) {
                        short[] expectedArray = expectedDigitalValues[i];
                        short[] actualArray = actualDigitalValues[i];
                        assertEquals(expectedArray.length, actualArray.length);
                        assertArrayEquals(expectedArray, actualArray);
                }
        }

        private EDFHeader buildHeader() {
                return new EDFAnnotationFileHeaderBuilder()
                        .startOfRecording(new Date()).durationOfRecord(1).numberOfSamples(new Integer[] { 100 })
                        .patientCode("1234").patientIsMale(true).patientBirthdate(new Date()).patientName("The patient")
                        .recordingHospital("Hosp.").recordingTechnician("Techn.").recordingEquipment("Equ.")
                        .channelLabels(new String[] { "EDF Annotations" }).transducerTypes(new String[] { "" })
                        .dimensions(new String[] { "" }).minInUnits(new Double[] { 0.0 }).maxInUnits(
                                new Double[] { 1.0 })
                        .digitalMin(new Integer[] { -32768 }).digitalMax(new Integer[] { 32767 })
                        .prefilterings(new String[] { "" }).numberOfSamples(new Integer[] { 100 })
                        .reserveds(new byte[1][EDFConstants.RESERVED_SIZE]).build();
        }

}
