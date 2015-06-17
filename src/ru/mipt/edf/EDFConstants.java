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

import java.nio.charset.Charset;

/**
 * This class contains constants for the EDF/EDF+ specification.
 */
class EDFConstants
{
        private EDFConstants() {}

        static final Charset CHARSET = Charset.forName("ASCII");

        static final int IDENTIFICATION_CODE_SIZE = 8;
        static final int LOCAL_SUBJECT_IDENTIFICATION_SIZE = 80;
        static final int LOCAL_REOCRDING_IDENTIFICATION_SIZE = 80;
        static final int START_DATE_SIZE = 8;
        static final int START_TIME_SIZE = 8;
        static final int HEADER_SIZE = 8;
        static final int DATA_FORMAT_VERSION_SIZE = 44;
        static final int DURATION_DATA_RECORDS_SIZE = 8;
        static final int NUMBER_OF_DATA_RECORDS_SIZE = 8;
        static final int NUMBER_OF_CHANELS_SIZE = 4;

        static final int LABEL_OF_CHANNEL_SIZE = 16;
        static final int TRANSDUCER_TYPE_SIZE = 80;
        static final int PHYSICAL_DIMENSION_OF_CHANNEL_SIZE = 8;
        static final int PHYSICAL_MIN_IN_UNITS_SIZE = 8;
        static final int PHYSICAL_MAX_IN_UNITS_SIZE = 8;
        static final int DIGITAL_MIN_SIZE = 8;
        static final int DIGITAL_MAX_SIZE = 8;
        static final int PREFILTERING_SIZE = 80;
        static final int NUMBER_OF_SAMPLES_SIZE = 8;
        static final int RESERVED_SIZE = 32;

        /** The size of the EDF-Header-Record containing information about the recording */
        static final int HEADER_SIZE_RECORDING_INFO
                = IDENTIFICATION_CODE_SIZE + LOCAL_SUBJECT_IDENTIFICATION_SIZE + LOCAL_REOCRDING_IDENTIFICATION_SIZE
                  + START_DATE_SIZE + START_TIME_SIZE + HEADER_SIZE + DATA_FORMAT_VERSION_SIZE + DURATION_DATA_RECORDS_SIZE
                  + NUMBER_OF_DATA_RECORDS_SIZE + NUMBER_OF_CHANELS_SIZE;

        /** The size per channel of the EDF-Header-Record containing information a channel of the recording */
        static final int HEADER_SIZE_PER_CHANNEL
                = LABEL_OF_CHANNEL_SIZE + TRANSDUCER_TYPE_SIZE + PHYSICAL_DIMENSION_OF_CHANNEL_SIZE
                  + PHYSICAL_MIN_IN_UNITS_SIZE + PHYSICAL_MAX_IN_UNITS_SIZE + DIGITAL_MIN_SIZE + DIGITAL_MAX_SIZE
                  + PREFILTERING_SIZE + NUMBER_OF_SAMPLES_SIZE + RESERVED_SIZE;
}
