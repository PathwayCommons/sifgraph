package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Searches common downstream or common upstream of a specified set of entities
 * based on the given direction within the boundaries of a specified length
 * limit. Takes a source set of entities, direction of the query and
 * distance limit.
 *
 * @author Ozgun Babur
 */
public class CommonStream extends AbstractAlgorithm
{
	/**
	 * Collection of Set of nodes.
	 * Each Set contains all states of corresponding physical entity or
	 * contains one of the selected nodes
	 */
	private Collection<Set<String>> sourceSet;

	private static final String REACH_COUNT_LABEL = "REACH_COUNT_LABEL";

	/**
	 * Constructor for Common Stream with Selected Nodes.
	 */
	public CommonStream(SIFGraph graph, EdgeSelector edgeSelector, Set<String> sourceNodeSet, Direction direction,
		int limit)
	{
		super(graph, edgeSelector, null, direction, limit);
		this.sourceSet = new LinkedHashSet<>();

		//Each set contains only one selected Node
		for (String node : sourceNodeSet)
		{
			Set<String> sourceNode = new HashSet<>();
			sourceNode.add(node);
			sourceSet.add(sourceNode);
		}

		init(direction);
	}

	/**
	 * Constructor for Common Stream with Entity States.
	 */
	public CommonStream(SIFGraph graph, EdgeSelector edgeSelector, Collection<Set<String>> sourceStateSet,
		Direction direction, int limit)
	{
		super(graph, edgeSelector, null, direction, limit);
		this.sourceSet = sourceStateSet;
		init(direction);
	}

	private void init(Direction direction)
	{
		setDefaultUnlabeled(0);

		if (direction != Direction.UPSTREAM && direction != Direction.DOWNSTREAM)
		{
			throw new IllegalArgumentException("The direction has to be either UPSTREAM or DOWNSTREAM. Other values " +
				"do not have a meaning for this query.");
		}
	}

	/**
	 * Method to run query
	 */
	public Set<String> run()
	{
		/**
		 * Candidate contains all the graph objects that are the results of BFS.
		 * Eliminating nodes from candidate according to the reached counts
		 * will yield result.
		 */
		Map<Object, Integer> candidate = new HashMap<>();
		Set<Object> result = new HashSet<>();

		//for each set of states of entity, run BFS separately
		for (Set<String> source : sourceSet)
		{
			//run BFS for set of states of each entity
			BFS bfs = new BFS (graph, edgeSelector, source, null, direction, limit);
			Map<Object, Integer> bfsResult = bfs.run();

			/**
			 * Reached counts of the graph objects that are in BFSResult will
			 * be incremented by 1.
			 */
			for (Object go : bfsResult.keySet())
			{
				putLabel(go, REACH_COUNT_LABEL, (getLabel(go, REACH_COUNT_LABEL) + 1));
			}

			//put BFS Result into candidate set
			candidate.putAll(bfsResult);
		}

		/**
		 * Having a reached count equal to number of nodes in the source set
		 * indicates being in common stream.
		 */
		for(Object go : candidate.keySet())
		{
			if (getLabel(go, REACH_COUNT_LABEL) == sourceSet.size())
			{
				result.add(go);
			}
		}

		Set<String> queryResult = new HashSet<>();

		//Take out Nodes and store it to result
		for (Object go : result)
		{
			if (go instanceof String)
			{
				queryResult.add((String) go);
			}
		}

//		queryResult.removeAll(sourceSet.stream().flatMap(Collection::stream).collect(Collectors.toSet()));

		//Return the result of query
		return queryResult;
	}
}
