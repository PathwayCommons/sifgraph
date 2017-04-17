package org.pathwaycommons.sif.query;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFGraph;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.junit.Assert.*;
import static org.pathwaycommons.sif.util.Separator.*;

/**
 * @author Ozgun Babur
 */
public class NeighborhoodTest
{
	@Test
	public void testRun() throws Exception
	{
		SIFGraph graph = Mocker.mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);
		Neighborhood query = new Neighborhood(graph, edge -> true, new HashSet<>(asList("S", "T")), Direction.BOTHSTREAM, 1);
		assertEquals(getNodes(query.run()),
			new HashSet<>(asList("S", "T", "1", "2", "3", "4", "5", "6", "9", "10")));

		query = new Neighborhood(graph, edge -> true, new HashSet<>(asList("S", "T")), Direction.UPSTREAM, 1);
		assertEquals(getNodes(query.run()),
			new HashSet<>(asList("S", "T", "1", "2", "3", "4", "10")));

		query = new Neighborhood(graph, edge -> true, new HashSet<>(asList("S", "T")), Direction.DOWNSTREAM, 1);
		assertEquals(getNodes(query.run()),
			new HashSet<>(asList("S", "T", "1", "2", "5", "6", "9")));

		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.UNDIRECTED, 2);
		assertEquals(getNodes(query.run()),
			new HashSet<>(asList("S", "T", "1", "2", "3", "5", "7", "8", "9")));

		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.UNDIRECTED, 3);
		assertEquals(getNodes(query.run()),
			new HashSet<>(asList("S", "T", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10")));

		query = new Neighborhood(graph, edge -> true, singleton("T"), Direction.BOTHSTREAM, 2);
		assertEquals(false, getNodes(query.run()).contains("11"));

		query = new Neighborhood(graph, edge -> true, singleton("T"), Direction.UNDIRECTED, 2);
		assertEquals(true, getNodes(query.run()).contains("11"));

		// Undirected graph

		graph = Mocker.mockGraph1(RelationTypeEnum.IN_COMPLEX_WITH);
		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.UPSTREAM, 1);
		assertEquals(new HashSet<>(asList("S", "1", "2", "3", "5", "9")), getNodes(query.run()));

		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.DOWNSTREAM, 1);
		assertEquals(new HashSet<>(asList("S", "1", "2", "3", "5", "9")), getNodes(query.run()));

		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.BOTHSTREAM, 1);
		assertEquals(new HashSet<>(asList("S", "1", "2", "3", "5", "9")), getNodes(query.run()));

		query = new Neighborhood(graph, edge -> true, singleton("S"), Direction.UNDIRECTED, 1);
		assertEquals(new HashSet<>(asList("S", "1", "2", "3", "5", "9")), getNodes(query.run()));
	}
}