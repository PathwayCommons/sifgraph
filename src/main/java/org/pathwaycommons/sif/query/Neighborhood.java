package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.pathwaycommons.sif.query.Direction.*;

/**
 * @author Ozgun Babur
 */
public class Neighborhood extends AbstractAlgorithm
{
	/**
	 * Constructor for Neighborhood Query.
	 */
	public Neighborhood(SIFGraph graph, EdgeSelector selector, Set<String> sourceNodes, Direction direction, int limit)
	{
		super(graph, selector, sourceNodes, direction, limit);
	}

	/**
	 * Method to run query
	 */
	public Set<Object> run()
	{
		Set<Object> queryResult = new HashSet<>();

		//if upstream is selected
		if (direction == UPSTREAM || direction == BOTHSTREAM)
		{
			//run BFS in upstream direction
			BFS bfsBackward = new BFS(graph, edgeSelector, sourceNodes, null, UPSTREAM, limit);

			Map<Object, Integer> mapBackward = bfsBackward.run();

			//add result of BFS to result Set
			queryResult.addAll(mapBackward.keySet());
		}
		//if downstream is selected
		if (direction == DOWNSTREAM || direction == BOTHSTREAM)
		{
			//run BFS in downstream direction
			BFS bfsForward = new BFS(graph, edgeSelector, sourceNodes, null, DOWNSTREAM, limit);

			Map<Object, Integer> mapForward = bfsForward.run();

			//add result of BFS to result Set
			queryResult.addAll(mapForward.keySet());
		}

		if (direction == UNDIRECTED)
		{
			// The source set that will enlarge at each round of BFS
			Set<String> source = new HashSet<>(sourceNodes);

			for (int i = 0; i < limit; i++)
			{
				BFS bfsBackward = new BFS(graph, edgeSelector, source, null, UPSTREAM, 1);
				Map<Object, Integer> mapBackward = bfsBackward.run();
				queryResult.addAll(mapBackward.keySet());

				BFS bfsForward = new BFS(graph, edgeSelector, source, null, DOWNSTREAM, 1);
				Map<Object, Integer> mapForward = bfsForward.run();
				queryResult.addAll(mapForward.keySet());

				// add the nodes in the result to the source set for the next round
				queryResult.stream().filter(o -> o instanceof String).map(o -> (String) o).forEach(source::add);
			}
		}

		return queryResult;
	}
}
