package test.ru.mipt.edf;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

import org.junit.Test;

import ru.mipt.edf.EDFAnnotationFileHeaderBuilder;
import ru.mipt.edf.EDFHeader;
import ru.mipt.edf.EDFParser;
import ru.mipt.edf.EDFWriter;

public class EDFAnnotationsWriterTest {

	@Test
	public void writeAndReadHeaderShouldReturnTheSameHeader() throws IOException {
		
		EDFHeader header = new EDFAnnotationFileHeaderBuilder()
			.startOfRecording(new Date()).durationOfRecord(1000).numberOfSamples(100)
			.patientCode("1234").patientIsMale(true).patientBirthdate(new Date()).patientName("The patient")
			.recordingHospital("Hosp.").recordingTechnician("Techn.").recordingEquipment("Equ.").build();

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		EDFWriter.writeIntoOutputStream(header, out);
		
		EDFHeader parsedHeader = EDFParser.parseHeader(new ByteArrayInputStream(out.toByteArray())).getHeader();
		
		assertEquals(header.getIdCode(), parsedHeader.getIdCode().trim());
		assertEquals(header.getSubjectID(), parsedHeader.getSubjectID().trim());
		assertEquals(header.getRecordingID(), parsedHeader.getRecordingID().trim());
		assertEquals(header.getStartDate(), parsedHeader.getStartDate());
		assertEquals(header.getStartTime(), parsedHeader.getStartTime());
		assertEquals(header.getBytesInHeader(), parsedHeader.getBytesInHeader());
		assertEquals(header.getFormatVersion(), parsedHeader.getFormatVersion().trim());
		assertEquals(header.getNumberOfRecords(), parsedHeader.getNumberOfRecords());
		assertEquals(0, Double.compare(header.getDurationOfRecords(), parsedHeader.getDurationOfRecords()));
		assertEquals(header.getNumberOfChannels(), parsedHeader.getNumberOfChannels());
		
		for (int i=0; i < header.getNumberOfChannels(); i++) {
			assertEquals(header.getChannelLabels()[i], parsedHeader.getChannelLabels()[i].trim());
			assertEquals(header.getTransducerTypes()[i], parsedHeader.getTransducerTypes()[i].trim());
			assertEquals(header.getDimensions()[i], parsedHeader.getDimensions()[i].trim());
			assertEquals(0, Double.compare(header.getMinInUnits()[i], parsedHeader.getMinInUnits()[i]));
			assertEquals(0, Double.compare(header.getMaxInUnits()[i], parsedHeader.getMaxInUnits()[i]));
			assertEquals(header.getDigitalMin()[i], parsedHeader.getDigitalMin()[i]);
			assertEquals(header.getDigitalMax()[i], parsedHeader.getDigitalMax()[i]);
			assertEquals(header.getPrefilterings()[i], parsedHeader.getPrefilterings()[i].trim());
			assertEquals(header.getNumberOfSamples()[i], parsedHeader.getNumberOfSamples()[i]);
			assertArrayEquals(header.getReserveds()[i], parsedHeader.getReserveds()[i]);
		}
	}
}
