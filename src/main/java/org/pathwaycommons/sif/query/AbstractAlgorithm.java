package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class AbstractAlgorithm
{
	/**
	 * The graph that the query will run on.
	 */
	protected SIFGraph graph;

	/**
	 * For labeling graph objects.
	 */
	private Map<Object, Map<String, Integer>> objectLabelMap;

	/**
	 * Default return value for something unlabeled.
	 */
	private int defaultUnlabeled = Integer.MAX_VALUE - 1;

	/**
	 * Selects which edges will be considered in the query.
	 */
	protected EdgeSelector edgeSelector;

	/**
	 * Set of source/seed nodes.
	 */
	protected Set<String> sourceNodes;

	/**
	 * The direction of the query, if applicable.
	 */
	protected Direction direction;

	protected int limit;

	public AbstractAlgorithm(SIFGraph graph, EdgeSelector edgeSelector, Set<String> sourceNodes, Direction direction,
		int limit)
	{
		this.graph = graph;
		this.edgeSelector = edgeSelector;
		this.limit = limit;
		this.sourceNodes = sourceNodes;
		this.direction = direction;
		objectLabelMap = new HashMap<>();
	}

	public void setDefaultUnlabeled(int defaultUnlabeled)
	{
		this.defaultUnlabeled = defaultUnlabeled;
	}

	protected void putLabel(Object go, String label, Integer value)
	{
		if (!objectLabelMap.containsKey(go))
		{
			objectLabelMap.put(go, new HashMap<>());
		}

		objectLabelMap.get(go).put(label, value);
	}

	protected void removeLabel(Object go, String label)
	{
		if (objectLabelMap.containsKey(go))
		{
			objectLabelMap.get(go).remove(label);
		}
	}

	protected void clearAllLabels()
	{
		objectLabelMap.clear();
	}

	protected void clearLabel(String label)
	{
		for (Object go : objectLabelMap.keySet())
		{
			objectLabelMap.get(go).remove(label);
		}
	}

	protected boolean hasLabel(Object go, String label)
	{
		return objectLabelMap.containsKey(go) && objectLabelMap.get(go).containsKey(label);
	}

	protected int getLabel(Object go, String label)
	{
		if (hasLabel(go, label)) return objectLabelMap.get(go).get(label);
		else return defaultUnlabeled;
	}

}
