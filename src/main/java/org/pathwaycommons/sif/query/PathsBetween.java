package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFEdge;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;

/**
 * @author Ozgun Babur
 */
public class PathsBetween extends AbstractAlgorithm
{
	private Set<String> targetNodes;
	private Set<String> allSeed;
	private boolean directed;

	/**
	 * If false, then any path in length limit will come. If true, shortest+k limit will be used,
	 * again bounded by limit.
	 */
	private boolean useShortestPlusK = false;

	/**
	 * If true, will ignore cycles.
	 */
	private boolean ignoreSelfLoops = true;

	/**
	 * If true, then a shortest path will be considered for each distinct pair. If false, then a
	 * shortest path length per gene will be used.
	 */
	private boolean considerAllPairs = false;
	/**
	 * If true, and if the reverse path is longer, it wont be retrieved.
	 */
	private boolean shortestAnyDir = true;
	private Map<String, Map<String, Integer>> shortestPairLengths;
	private Map<String, Integer> shortestSingleLengths;
	private int k = 0;

	Set<Object> goi;

	private Map<Object, Map<Integer, Set<String>>> fwdLabel;
	private Map<Object, Map<Integer, Set<String>>> bkwLabel;
	private Map<Object, Map<Integer, Set<String>>> labelMap;

	private Set<Object> visitedGlobal;

	public PathsBetween(SIFGraph graph, EdgeSelector selector, Set<String> seed, boolean directed, int limit)
	{
		this(graph, selector, seed, seed, directed, limit);
	}

	public PathsBetween(SIFGraph graph, EdgeSelector selector, Set<String> sourceSeed, Set<String> targetNodes,
		boolean directed, int limit)
	{
		super(graph, selector, sourceSeed, null, limit);
		this.targetNodes = targetNodes;
		this.allSeed = Stream.concat(sourceSeed.stream(), targetNodes.stream()).collect(Collectors.toSet());
		this.directed = directed;
	}

	public void setUseShortestPlusK(boolean useShortestPlusK)
	{
		this.useShortestPlusK = useShortestPlusK;
	}

	public void setIgnoreSelfLoops(boolean ignoreSelfLoops)
	{
		this.ignoreSelfLoops = ignoreSelfLoops;
	}

	public void setConsiderAllPairs(boolean considerAllPairs)
	{
		this.considerAllPairs = considerAllPairs;
	}

	public void setShortestAnyDir(boolean shortestAnyDir)
	{
		this.shortestAnyDir = shortestAnyDir;
	}

	public void setShortestPairLengths(Map<String, Map<String, Integer>> shortestPairLengths)
	{
		this.shortestPairLengths = shortestPairLengths;
	}

	public void setK(int k)
	{
		this.k = k;
	}

	public Set<Object> run()
	{
		goi = new HashSet<>();
		visitedGlobal = new HashSet<>();

		if (directed)
		{
			this.fwdLabel = new HashMap<>();
			this.bkwLabel = new HashMap<>();
		}
		else this.labelMap = new HashMap<>();

		for (String node : sourceNodes)
		{
			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, Direction.DOWNSTREAM);
			}
			else
			{
				runBFS_undirected(node);
			}

			// Record distances for that seed node
			recordDistances(node);

			// Remove all algorithm specific labels
			clearLabels();
		}

		for (String node : targetNodes)
		{
			if (!directed && sourceNodes.contains(node)) continue;

			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, Direction.UPSTREAM);
			}
			else
			{
				runBFS_undirected(node);
			}

			// Record distances for that seed node
			recordDistances(node);

			// Remove all algorithm specific labels
			clearLabels();
		}

		if (useShortestPlusK) findShortestPaths();

		// Reformat the label maps

		if (directed)
		{
			mergeLabels(fwdLabel);
			mergeLabels(bkwLabel);
		}
		else mergeLabels(labelMap);

		// Select graph objects that are traversed with the BFS. It is important to process nodes
		// before edges.
		selectSatisfyingElements();

		// Prune so that no non-seed degree-1 nodes remain
		pruneResult();

		assert checkEdgeSanity();

		return goi;
	}

	private void runBFS_directed(String seed, Direction direction)
	{
		assert directed;

		Set<String> stopSet = ignoreSelfLoops ? allSeed : null;

		BFS bfs = new BFS(graph, edgeSelector, singleton(seed), stopSet, direction, limit);
		Map<Object, Integer> distMap = bfs.run();

		String label = direction == Direction.DOWNSTREAM ? DIST_FORWARD : DIST_BACKWARD;

		for (Object go : distMap.keySet())
		{
			setLabel(go, label, distMap.get(go));
		}
	}

	private void runBFS_undirected(String seed)
	{
		assert !directed;

		Set<String> stopSet = ignoreSelfLoops ? allSeed : null;

		BFS bfs = new BFS(graph, edgeSelector, singleton(seed), stopSet, Direction.UNDIRECTED, limit);
		Map<Object, Integer> distMap = bfs.run();

		for (Object go : distMap.keySet())
		{
			setLabel(go, DIST, distMap.get(go));
		}
	}

	private void initSeed(Object obj)
	{
		if (directed)
		{
			setLabel(obj, DIST_FORWARD, 0);
			setLabel(obj, DIST_BACKWARD, 0);
		}
		else
		{
			setLabel(obj, DIST, 0);
		}
	}

	private void selectSatisfyingElements()
	{
		for (Object go : visitedGlobal)
		{
			if (distanceSatisfies(go))
			{
				goi.add(go);
			}
		}

		// Remove edges in the result whose node is not in the result

		Set<SIFEdge> extra = new HashSet<>();
		for (Object go : goi)
		{
			if (go instanceof SIFEdge)
			{
				SIFEdge edge = (SIFEdge) go;
				if (!goi.contains(edge.getSource()) || !goi.contains(edge.getTarget()))
				{
					extra.add(edge);
				}
			}
		}
		goi.removeAll(extra);
	}

	private boolean distanceSatisfies(Object go)
	{
		if (directed)
		{
			return this.distanceSatisfies(go, fwdLabel, bkwLabel);
		}
		else
		{
			return this.distanceSatisfies(go, labelMap, labelMap);

			// just to remember old

//			if (!labelMap.containsKey(go)) return false;
//
//			for (Integer i : labelMap.get(go).keySet())
//			{
//				if (i <= limit && labelMap.get(go).get(i).size() > 1) return true;
//			}
//			return false;
		}
	}

	private boolean distanceSatisfies(Object go,
		Map<Object, Map<Integer, Set<String>>> fwdLabel,
		Map<Object, Map<Integer, Set<String>>> bkwLabel)
	{
		if (!fwdLabel.containsKey(go) || !bkwLabel.containsKey(go)) return false;

		for (Integer i : fwdLabel.get(go).keySet())
		{
			for (Integer j : bkwLabel.get(go).keySet())
			{
				int dist = i + j;

				if (!directed && go instanceof SIFEdge) dist--;

				if (dist <= limit)
				{
					if (setsSatisfy(fwdLabel.get(go).get(i), bkwLabel.get(go).get(j), dist))
						return true;
				}
			}
		}
		return false;
	}

	private void pruneResult()
	{
		for (Object go : new HashSet<>(goi))
		{
			if (go instanceof String)
			{
				prune((String) go);
			}
		}
	}

	private void prune(String node)
	{
		if (goi.contains(node) && !(sourceNodes.contains(node) || targetNodes.contains(node)))
		{
			if (getNeighborsInResult(node).size() <= 1)
			{
				goi.remove(node);
				goi.removeAll(graph.getUpstream(node));
				goi.removeAll(graph.getDownstream(node));

				for (String n : getNeighborsOverResultEdges(node))
				{
					prune(n);
				}
			}
		}
	}


	private Set<String> getNeighborsOverResultEdges(String node)
	{
		Set<String> set = graph.getUpstream(node).stream()
			.filter(edge -> goi.contains(edge))
			.map(SIFEdge::getSource).collect(Collectors.toSet());

		set.addAll(graph.getDownstream(node).stream()
			.filter(edge -> goi.contains(edge))
			.map(SIFEdge::getTarget).collect(Collectors.toList()));

		set.remove(node);
		return set;
	}

	private Set<String> getNeighborsInResult(String node)
	{
		Set<String> set = getNeighborsOverResultEdges(node);
		set.retainAll(goi);
		return set;
	}

	private void clearLabels()
	{
		if (directed)
		{
			clearLabel(DIST_FORWARD);
			clearLabel(DIST_BACKWARD);
		}
		else
		{
			clearLabel(DIST);
		}
	}

	private boolean checkEdgeSanity()
	{
		for (Object go : goi)
		{
			if (go instanceof SIFEdge)
			{
				SIFEdge edge = (SIFEdge) go;

				assert goi.contains(edge.getSource());
				assert goi.contains(edge.getTarget());
			}
		}
		return true;
	}

	private void setLabel(Object go, String label, Integer value)
	{
		putLabel(go, label, value);
		visitedGlobal.add(go);
	}


	private void recordDistances(String seed)
	{
		for (Object go : visitedGlobal)
		{
			if (directed)
			{
				recordDistance(go, seed, DIST_FORWARD, fwdLabel);
				recordDistance(go, seed, DIST_BACKWARD, bkwLabel);
			}
			else recordDistance(go, seed, DIST, labelMap);
		}
	}

	private void recordDistance(Object go, String seed, String label, Map<Object, Map<Integer, Set<String>>> map)
	{
		int d = getLabel(go, label);
		if (d > limit) return;
		if (!map.containsKey(go)) map.put(go, new HashMap<>());
		if (!map.get(go).containsKey(d)) map.get(go).put(d, new HashSet<>());
		map.get(go).get(d).add(seed);
	}

	private void mergeLabels(Map<Object, Map<Integer, Set<String>>> map)
	{
		for (Object go : map.keySet())
		{
			for (int i = 0; i < limit; i++)
			{
				if (map.get(go).containsKey(i))
				{
					for (int j = i+1; j <= limit; j++)
					{
						if (map.get(go).containsKey(j))
						{
							map.get(go).get(j).addAll(map.get(go).get(i));
						}
					}
				}
			}
		}
	}

	private boolean setsSatisfy(Set<String> set1, Set<String> set2, int length)
	{
		assert !set1.isEmpty();
		assert !set2.isEmpty();

		if (useShortestPlusK)
		{
			for (String source : set1)
			{
				for (String target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (!sourceNodes.contains(source) || !targetNodes.contains(target)) continue;

					if ((considerAllPairs && shortestPairLengths.containsKey(source) &&
						shortestPairLengths.get(source).containsKey(target)) ||
						(!considerAllPairs && shortestSingleLengths.containsKey(source) &&
							shortestSingleLengths.containsKey(target)))
					{
						// decide limit
						int limit;

						if (considerAllPairs)
						{
							limit = shortestPairLengths.get(source).get(target);
							if (shortestAnyDir && shortestPairLengths.containsKey(target) &&
								shortestPairLengths.get(target).containsKey(source))
							{
								limit = Math.min(limit, shortestPairLengths.get(target).get(source));
							}
						}
						else
						{
							limit = Math.max(shortestSingleLengths.get(source),
								shortestSingleLengths.get(target));
						}

						limit = Math.min(limit + k, this.limit);

						if (limit >= length) return true;
					}
				}
			}
			return false;
		}
		else
		{
			for (String source : set1)
			{
				for (String target : set2)
				{
					if (ignoreSelfLoops && source.equals(target)) continue;
					if (sourceNodes.contains(source) && targetNodes.contains(target)) return true;
				}
			}
			return false;
		}
	}

	private void findShortestPaths()
	{
		if (directed) this.findShortestPaths(fwdLabel, bkwLabel);
		else this.findShortestPaths(labelMap, labelMap);
	}

	private void findShortestPaths(Map<Object, Map<Integer, Set<String>>> fwdLabel,
		Map<Object, Map<Integer, Set<String>>> bkwLabel)
	{
		if (considerAllPairs) shortestPairLengths = new HashMap<>();
		else shortestSingleLengths = new HashMap<>();

		for (Object go : fwdLabel.keySet())
		{
			if (go instanceof SIFEdge) continue;

			Map<Integer, Set<String>> fwMap = fwdLabel.get(go);
			Map<Integer, Set<String>> bwMap = bkwLabel.get(go);

			if (fwMap == null || bwMap == null) continue;

			for (Integer d1 : fwMap.keySet())
			{
				for (String source : fwMap.get(d1))
				{
					for (Integer d2 : bwMap.keySet())
					{
						if (d1 + d2 > limit) continue;

						for (String target : bwMap.get(d2))
						{
							if (ignoreSelfLoops && source.equals(target)) continue;

							if (considerAllPairs)
							{
								if (!shortestPairLengths.containsKey(source))
								{
									shortestPairLengths.put(source, new HashMap<>());
								}

								if (!shortestPairLengths.get(source).containsKey(target) ||
									shortestPairLengths.get(source).get(target) > d1 + d2)
								{
									shortestPairLengths.get(source).put(target, d1 + d2);
								}
							}
							else
							{
								if (!shortestSingleLengths.containsKey(source) ||
									shortestSingleLengths.get(source) > d1 + d2)
								{
									shortestSingleLengths.put(source, d1 + d2);
								}
								if (!shortestSingleLengths.containsKey(target) ||
									shortestSingleLengths.get(target) > d1 + d2)
								{
									shortestSingleLengths.put(target, d1 + d2);
								}
							}
						}
					}
				}
			}
		}
	}



	public static final String DIST = "DIST";
	public static final String DIST_FORWARD = "DIST_FORWARD";
	public static final String DIST_BACKWARD = "DIST_BACKWARD";

	public static final boolean FORWARD = true;
	public static final boolean BACKWARD = false;
	public static final boolean UPSTREAM = true;
	public static final boolean DOWNSTREAM = false;
}
