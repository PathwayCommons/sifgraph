package org.pathwaycommons.sif.io;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.query.Mocker;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * @author Ozgun Babur
 */
public class WriterTest
{

	@Test
	public void testWrite() throws Exception
	{
		SIFGraph graph = Mocker.mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);
		Writer writer = new Writer(false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(graph, baos);
		String relsString = baos.toString();
		assertEquals(true, relsString.split("\n").length == 15);

		graph.addNodeAnnotation("S", "S\tSource\tSome description");

		writer = new Writer(false);
		baos = new ByteArrayOutputStream();
		writer.write(graph, baos);
		relsString = baos.toString();
		assertEquals(true, relsString.split("\n").length == 15);

		writer = new Writer(true);
		baos = new ByteArrayOutputStream();
		writer.write(graph, baos);
		relsString = baos.toString();
		assertEquals(true, relsString.split("\n").length == 17);
	}
}