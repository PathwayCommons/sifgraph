package org.pathwaycommons.sif.query;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFGraph;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;
import static org.pathwaycommons.sif.util.Separator.*;

/**
 * @author Ozgun Babur
 */
public class PathsBetweenTest
{

	@Test
	public void testRun() throws Exception
	{
		SIFGraph graph = Mocker.mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);

		// paths between

		Set<String> seed = new HashSet<>(asList("S", "T"));

		PathsBetween query = new PathsBetween(graph, edge -> true, seed, true, 2);
		assertEquals(new HashSet<>(asList("S", "T", "1", "2")), getNodes(query.run()));

		// paths from to

		query = new PathsBetween(graph, edge -> true, singleton("S"), singleton("T"), true, 2);
		assertEquals(new HashSet<>(asList("S", "T", "1")), getNodes(query.run()));

		// undirected

		query = new PathsBetween(graph, edge -> true, singleton("S"), singleton("T"), false, 3);
		assertEquals(new HashSet<>(asList("S", "T", "1", "2")), getNodes(query.run()));

		query = new PathsBetween(graph, edge -> true, seed, false, 4);
//		query.setIgnoreSelfLoops(false);
		assertEquals(new HashSet<>(asList("S", "T", "1", "2", "3", "4", "5", "6", "7", "8")), getNodes(query.run()));

	}
}