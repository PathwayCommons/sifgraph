package org.pathwaycommons.sif.query;

/**
 * Why is BOTHSTREAM different than UNDIRECTED? Consider the below graph
 *
 * A --> X
 * B --> X
 *
 * There is a path between A and B of length 2 only if the direction is UNDIRECTED. There is no path in any of other
 * cases. BOTHSTREAM means either UPSTREAM or DOWNSTREAM, but has to be consistent. They cannot be mixed on a path. This
 * makes a difference when distance limit is more than 1.
 *
 * @author Ozgun Babur
 */
public enum Direction
{
	UPSTREAM,
	DOWNSTREAM,
	BOTHSTREAM,
	UNDIRECTED
}
