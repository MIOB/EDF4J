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

/**
 * This exception is thrown if the file format is not according to EDF.
 */
public class EDFParserException extends IOException
{
        private static final long serialVersionUID = 3807109927368496625L;

        public EDFParserException()
        {
                this("File format not according to EDF/EDF+ specification.", null);
        }

        public EDFParserException(Throwable th)
        {
                this("File format not according to EDF/EDF+ specification.", th);
        }

        public EDFParserException(String message, Throwable th)
        {
                super(message, th);
        }
}
