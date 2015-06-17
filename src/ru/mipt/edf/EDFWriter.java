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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import static ru.mipt.edf.EDFConstants.*;

/**
 * This class is capable of writing EDF+ data structures.
 * Changed for fixing issue #3 from Github: https://github.com/MIOB/EDF4J/issues/3
 */
public class EDFWriter
{
        public static final String SHORT_DECIMAL_FORMAT = "#0.0";
        public static final String LONG_DECIMAL_FORMAT = "#0.0####";

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
                DecimalFormat shortFormatter = new DecimalFormat(SHORT_DECIMAL_FORMAT, dfs);
                DecimalFormat longFormatter = new DecimalFormat(LONG_DECIMAL_FORMAT, dfs);

                ByteBuffer bb = ByteBuffer.allocate(header.bytesInHeader);
                putIntoBuffer(bb, IDENTIFICATION_CODE_SIZE, header.idCode);
                putIntoBuffer(bb, LOCAL_SUBJECT_IDENTIFICATION_SIZE, header.subjectID);
                putIntoBuffer(bb, LOCAL_REOCRDING_IDENTIFICATION_SIZE, header.recordingID);
                putIntoBuffer(bb, START_DATE_SIZE, header.startDate);
                putIntoBuffer(bb, START_TIME_SIZE, header.startTime);
                putIntoBuffer(bb, HEADER_SIZE, header.bytesInHeader);
                putIntoBuffer(bb, DATA_FORMAT_VERSION_SIZE, header.formatVersion);
                putIntoBuffer(bb, NUMBER_OF_DATA_RECORDS_SIZE, header.numberOfRecords);
                putIntoBuffer(bb, DURATION_DATA_RECORDS_SIZE, header.durationOfRecords, longFormatter);
                putIntoBuffer(bb, NUMBER_OF_CHANELS_SIZE, header.numberOfChannels);

                putIntoBuffer(bb, LABEL_OF_CHANNEL_SIZE, header.channelLabels);
                putIntoBuffer(bb, TRANSDUCER_TYPE_SIZE, header.transducerTypes);
                putIntoBuffer(bb, PHYSICAL_DIMENSION_OF_CHANNEL_SIZE, header.dimensions);
                putIntoBuffer(bb, PHYSICAL_MIN_IN_UNITS_SIZE, header.minInUnits, shortFormatter);
                putIntoBuffer(bb, PHYSICAL_MAX_IN_UNITS_SIZE, header.maxInUnits, shortFormatter);
                putIntoBuffer(bb, DIGITAL_MIN_SIZE, header.digitalMin);
                putIntoBuffer(bb, DIGITAL_MAX_SIZE, header.digitalMax);
                putIntoBuffer(bb, PREFILTERING_SIZE, header.prefilterings);
                putIntoBuffer(bb, NUMBER_OF_SAMPLES_SIZE, header.numberOfSamples);
                putIntoBuffer(bb, header.reserveds);

                outputStream.write(bb.array());
        }

        /**
         * Write the signals in output stream
         *
         * @param edfSignal     The signals to write
         * @param header        The header of EDF file
         * @param outputStream  The OutputStream to write into
         * @throws IOException  Will be thrown if it is not possible to write into the outputStream
         */
        public static void writeIntoOutputStream(EDFSignal edfSignal, EDFHeader header, OutputStream outputStream)
                throws IOException {

                short[] data = buildDataArray(edfSignal.getDigitalValues(), header);
                writeIntoOutputStream(data, outputStream);
        }

        /**
         * Write signals data in output stream
         *
         * @param data          The signals in edf short array format
         * @param outputStream  The OutputStream to write into
         * @throws IOException  Will be thrown if it is not possible to write into the outputStream
         */
        public static void writeIntoOutputStream(short[] data, OutputStream outputStream)
                throws IOException {

                ByteBuffer bb = ByteBuffer.allocate(data.length * 2);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                putIntoBuffer(bb, data);
                outputStream.write(bb.array());
        }

        /**
         * Convert data signals from two dimensions format ( {channels} {time, samples} ) to one
         * dimension format ( channels {samples for each channel} grouped by time )
         *
         * @param digitalValues         The signals data in two dimensions format
         * @param header                The header of edf format
         * @return                      The signals data in one dimensions format
         */
        public static short[] buildDataArray(short[][] digitalValues, EDFHeader header) {
                int index;
                int totalDataLength = 0;
                int previousSamples = 0;
                int timeChunkLength = 0;

                // compute the total length of the new short array
                for (short[] digitalValue : digitalValues) {
                        totalDataLength += digitalValue.length;
                }
                short[] signalsData = new short[totalDataLength];


                for (Integer sample : header.getNumberOfSamples()) {
                        timeChunkLength += sample;
                }

                // build the signals array, which is a short one
                for (int channel = 0; channel < digitalValues.length; channel++) {
                        short[] channelValues = digitalValues[channel];
                        int noOfSamples = header.getNumberOfSamples()[channel];

                        for (int t = 0; t < header.getNumberOfRecords(); t++) {

                                for (int sample = 0; sample < noOfSamples; sample++) {
                                        short shortValue = channelValues[t * noOfSamples + sample];
                                        index = t * timeChunkLength + previousSamples + sample;
                                        signalsData[index] = shortValue;
                                }

                        }
                        previousSamples += noOfSamples;
                }
                return signalsData;
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
                while (valueBuffer.remaining() > 0) {
                        valueBuffer.put(" ".getBytes());
                }

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

        private static void putIntoBuffer(ByteBuffer bb, short[] values)
        {
                for (short val : values)
                {
                        bb.putShort(val);
                }
        }
}
