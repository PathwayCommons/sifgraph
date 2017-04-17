package org.pathwaycommons.sif.util;

import org.pathwaycommons.sif.model.SIFEdge;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * For separating a mix of graph objects.
 *
 * @author Ozgun Babur
 */
public class Separator
{
	/**
	 * Gets all String objects from the given object set.
	 */
	public static Set<String> getNodes(Set<Object> graphObjects)
	{
		if (graphObjects == null) return null;

		return graphObjects.stream()
			.filter(o -> o instanceof String)
			.map(o -> (String) o).collect(Collectors.toSet());
	}

	/**
	 * Gets all edges from the given object set.
	 */
	public static Set<SIFEdge> getEdges(Set<Object> graphObjects)
	{
		if (graphObjects == null) return null;

		return graphObjects.stream()
			.filter(o -> o instanceof SIFEdge)
			.map(o -> (SIFEdge) o).collect(Collectors.toSet());
	}
}
