package org.pathwaycommons.sif.query;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFGraph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;
import static org.pathwaycommons.sif.util.Separator.*;

/**
 * @author Ozgun Babur
 */
public class CommonStreamTest
{

	@Test
	public void testRun() throws Exception
	{
		SIFGraph graph = Mocker.mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);

		Set<String> seed = new HashSet<>(asList("S", "T"));
		CommonStream query = new CommonStream(graph, edge -> true, seed, Direction.UPSTREAM, 1);
		assertEquals(true, query.run().isEmpty());

		query = new CommonStream(graph, edge -> true, seed, Direction.UPSTREAM, 2);
		assertEquals(new HashSet<>(asList("S", "T", "7")), query.run());

		query = new CommonStream(graph, edge -> true, seed, Direction.DOWNSTREAM, 2);
		assertEquals(new HashSet<>(asList("S", "T", "8")), query.run());
	}
}