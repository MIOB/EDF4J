EDF4J
=====

This is a Java-Parser for the file formats EDF and EDF+.

License
-------

This project is licensed under the terms of the MIT license. See license.txt.

Usage
=====

The parser is available in the file EDFParser.java

Example usage
-------------

    String pathToEdfFile = "";
    InputStream is = new BufferedInputStream(new FileInputStream(new File(pathToEdfFile)));
    EDFParserResult result = EDFParser.parseEDF(is);

Example program
---------------

An example program is available in the file EDF.java
