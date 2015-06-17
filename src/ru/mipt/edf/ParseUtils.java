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
import java.util.Arrays;

abstract class ParseUtils
{
        public static String[] readBulkASCIIFromStream(InputStream is, int size, int length) throws IOException
        {
                String[] result = new String[length];
                for (int i = 0; i < length; i++)
                {
                        result[i] = readASCIIFromStream(is, size);
                }
                return result;
        }

        public static Double[] readBulkDoubleFromStream(InputStream is, int size, int length) throws IOException
        {
                Double[] result = new Double[length];
                for (int i = 0; i < length; i++)
                        result[i] = Double.parseDouble(readASCIIFromStream(is, size).trim());
                return result;
        }

        public static Integer[] readBulkIntFromStream(InputStream is, int size, int length) throws IOException
        {
                Integer[] result = new Integer[length];
                for (int i = 0; i < length; i++)
                        result[i] = Integer.parseInt(readASCIIFromStream(is, size).trim());
                return result;
        }

        public static String readASCIIFromStream(InputStream is, int size) throws IOException
        {
                int len;
                byte[] data = new byte[size];
                len = is.read(data);
                if (len != data.length)
                        throw new EDFParserException();
                return new String(data, EDFConstants.CHARSET);
        }

        public static <T> T[] removeElement(T[] array, int i)
        {
                if (i < 0)
                        return array;
                if (i == 0)
                        return Arrays.copyOfRange(array, 1, array.length);
                T[] result = Arrays.copyOfRange(array, 0, array.length - 1);
                System.arraycopy(array, i + 1, result, i + 1 - 1, array.length - (i + 1));
                return result;
        }
}
