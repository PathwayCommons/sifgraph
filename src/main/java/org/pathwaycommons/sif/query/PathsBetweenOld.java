package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFEdge;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class PathsBetweenOld extends AbstractAlgorithm
{
	private Set<String> sourceSeed;
	private Set<String> targetSeed;
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
	private Set<Object> visitedStep;

	public PathsBetweenOld(SIFGraph graph, EdgeSelector selector, Set<String> seed, boolean directed, int limit)
	{
		this(graph, selector, seed, seed, directed, limit);
	}

	public PathsBetweenOld(SIFGraph graph, EdgeSelector selector, Set<String> sourceSeed, Set<String> targetSeed, boolean directed, int limit)
	{
		super(graph, selector, null, null, limit);
		this.sourceSeed = sourceSeed;
		this.targetSeed = targetSeed;
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
		visitedStep = new HashSet<>();

		if (directed)
		{
			this.fwdLabel = new HashMap<>();
			this.bkwLabel = new HashMap<>();
		}
		else this.labelMap = new HashMap<>();

		for (String node : sourceSeed)
		{
			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, FORWARD);
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

		for (String node : targetSeed)
		{
			if (!directed && sourceSeed.contains(node)) continue;

			initSeed(node);

			if (directed)
			{
				runBFS_directed(node, BACKWARD);
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

	private void runBFS_directed(String seed, boolean direction)
	{
		assert directed;

		// Initialize queue to contain all seed nodes

		LinkedList<String> queue = new LinkedList<>();
		queue.add(seed);
		visitedStep.add(seed);

		// Run BFS forward or backward till queue is not empty

		while (!queue.isEmpty())
		{
			String node = queue.poll();
			BFS_directed(node, direction, queue);
		}
	}

	private void runBFS_undirected(String seed)
	{
		assert !directed;

		// Initialize queue to contain all seed nodes

		LinkedList<String> queue = new LinkedList<String>();
		queue.add(seed);
		visitedStep.add(seed);

		// Run BFS till queue is not empty

		while (!queue.isEmpty())
		{
			String node = queue.poll();
			BFS_undirected(node, queue);
		}
	}

	private void BFS_directed(String node, boolean forward, LinkedList<String> queue)
	{
		assert directed;

		if (forward)
		{
			BFStep(node, DOWNSTREAM, DIST_FORWARD, queue);
		}
		else
		{
			BFStep(node, UPSTREAM, DIST_BACKWARD, queue);
		}
	}

	private void BFStep(String node, boolean upstr, String label, LinkedList<String> queue)
	{
		int d = getLabel(node, label);

		if (d < limit)
		{
			for (SIFEdge edge : upstr? graph.getUpstream(node) : graph.getDownstream(node))
			{
				if (visitedStep.contains(edge)) continue;

				setLabel(edge, label, !upstr && label.equals(DIST_FORWARD) ? d + 1 : d);

				String n = upstr ? edge.getSource() : edge.getTarget();

				int d_n = getLabel(n, label);

				if (d_n > d + 1)
				{
					if (d + 1 < limit && !visitedStep.contains(n) && !queue.contains(n)
						&& (!ignoreSelfLoops || !(sourceSeed.contains(n) || targetSeed.contains(n))))
						queue.add(n);

					setLabel(n, label, d + 1);
				}
			}
		}
	}

	private void BFS_undirected(String node, LinkedList<String> queue)
	{
		assert !directed;

		BFStep(node, UPSTREAM, DIST, queue);
		BFStep(node, DOWNSTREAM, DIST, queue);
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

				if (!directed && go instanceof SIFEdge) dist++;

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
		if (goi.contains(node) && !(sourceSeed.contains(node) || targetSeed.contains(node)))
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
		for (Object go : visitedStep)
		{
			if (directed)
			{
				removeLabel(go, DIST_FORWARD);
				removeLabel(go, DIST_BACKWARD);
			}
			else
			{
				removeLabel(go, DIST);
			}
		}
		visitedStep.clear();
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
		visitedStep.add(go);
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

	private void recordDistance(Object go, String seed, String label,
		Map<Object, Map<Integer, Set<String>>> map)
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
					if (!sourceSeed.contains(source) || !targetSeed.contains(target)) continue;

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
					if (sourceSeed.contains(source) && targetSeed.contains(target)) return true;
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
