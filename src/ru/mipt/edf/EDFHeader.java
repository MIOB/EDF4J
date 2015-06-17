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

/**
 * This class represents the complete header of an EDF-File.
 */
public class EDFHeader
{
        String idCode = null;
        String subjectID = null;
        String recordingID = null;
        String startDate = null;
        String startTime = null;
        int bytesInHeader = 0;
        String formatVersion = null;
        int numberOfRecords = 0;
        double durationOfRecords = 0;
        int numberOfChannels = 0;
        String[] channelLabels = null;
        String[] transducerTypes = null;
        String[] dimensions = null;
        Double[] minInUnits = null;
        Double[] maxInUnits = null;
        Integer[] digitalMin = null;
        Integer[] digitalMax = null;
        String[] prefilterings = null;
        Integer[] numberOfSamples = null;
        byte[][] reserveds = null;

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

        public Double[] getMinInUnits()
        {
                return minInUnits;
        }

        public Double[] getMaxInUnits()
        {
                return maxInUnits;
        }

        public Integer[] getDigitalMin()
        {
                return digitalMin;
        }

        public Integer[] getDigitalMax()
        {
                return digitalMax;
        }

        public String[] getPrefilterings()
        {
                return prefilterings;
        }

        public Integer[] getNumberOfSamples()
        {
                return numberOfSamples;
        }

        public byte[][] getReserveds()
        {
                return reserveds;
        }

}
