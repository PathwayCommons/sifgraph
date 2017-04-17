package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.Set;

import static org.pathwaycommons.sif.query.Direction.*;

/**
 * One stop shop interface for graph queries. Some parameters of queries may not have been exposed here.
 *
 * @author Ozgun Babur
 */
public class QueryExecutor
{
	/**
	 * Neighborhood query.
	 *
	 * @param graph The graph
	 * @param edgeSelector Selects the set of edges visible to this query
	 * @param seed Starting point of query
	 * @param direction The direction of neighbors. Note that BOTHSTREAM and UNDIRECTED can give different results
	 *                     when limit > 1
	 * @param limit Neighbor distance limit
	 * @return the neighborhood
	 */
	public static Set<Object> searchNeighborhood(SIFGraph graph, EdgeSelector edgeSelector, Set<String> seed,
		Direction direction, int limit)
	{
		Neighborhood query = new Neighborhood(graph, edgeSelector, seed, direction, limit);
		return query.run();
	}

	/**
	 * Paths-between query.
	 *
	 * @param graph The graph
	 * @param edgeSelector Selects the set of edges visible to this query
	 * @param seed Starting point of the query
	 * @param directed Whether the paths between two seed nodes have to be directed. This is not about which edge type
	 *                    to use, but about whether the direction of a directed edge is important.
	 * @param limit Path length limit
	 * @return paths between seed nodes
	 */
	public static Set<Object> searchPathsBetween(SIFGraph graph, EdgeSelector edgeSelector, Set<String> seed,
		boolean directed, int limit)
	{
		PathsBetween query = new PathsBetween(graph, edgeSelector, seed, directed, limit);
		return query.run();
	}

	/**
	 * Paths-from-to query.
	 *
	 * @param graph The graph
	 * @param edgeSelector Selects the set of edges visible to this query
	 * @param sources Paths will start from these nodes
	 * @param targets Paths will end at those nodes
	 * @param limit Path length limit
	 * @return paths from sources to the targets
	 */
	public static Set<Object> searchPathsFromTo(SIFGraph graph, EdgeSelector edgeSelector, Set<String> sources,
		Set<String> targets, int limit)
	{
		PathsBetween query = new PathsBetween(graph, edgeSelector, sources, targets, true, limit);
		return query.run();
	}

	/**
	 * Common upstream or common downstream query.
	 * @param graph The graph
	 * @param edgeSelector Selects the set of edges visible to this query
	 * @param seed Seed of the query
	 * @param direction Either UPSTREAM or DOWNSTREAM
	 * @param limit Path length limit between seed and the common elements
	 * @return the common stream
	 */
	public static Set<Object> searchCommonStream(SIFGraph graph, EdgeSelector edgeSelector, Set<String> seed,
		Direction direction, int limit)
	{
		if (direction == BOTHSTREAM || direction == UNDIRECTED)
		{
			throw new IllegalArgumentException("CommonStream query direction has to be either UPSTREAM or DOWNSTREAM");
		}

		CommonStream csQuery = new CommonStream(graph, edgeSelector, seed, direction, limit);
		Set<String> result = csQuery.run();
		Set<String> from = direction == UPSTREAM ? result : seed;
		Set<String> to = direction == DOWNSTREAM ? result : seed;
		PathsBetween query = new PathsBetween(graph, edgeSelector, from, to, true, limit);
		return query.run();
	}
}
