package org.pathwaycommons.sif.io;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.query.Mocker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

/**
 * @author Ozgun Babur
 */
public class LoaderTest
{

	@Test
	public void testLoad() throws Exception
	{
		SIFGraph graph = Mocker.mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);
		Writer writer = new Writer(false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(graph, baos);
		String relsString = baos.toString();

		Loader loader = new Loader();
		SIFGraph loadedlGraph = loader.load(new ByteArrayInputStream(relsString.getBytes()));

		assertEquals(true, graph.getAllEdges().equals(loadedlGraph.getAllEdges()));
	}
}