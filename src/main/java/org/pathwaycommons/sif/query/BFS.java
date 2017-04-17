package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.model.SIFEdge;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeSelector;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Basic breadth first search.
 *
 * @author Ozgun Babur
 */
public class BFS extends AbstractAlgorithm
{
	/**
	 * Distance labels. Missing label interpreted as infinitive.
	 */
	private Map<Object, Integer> dist;

	/**
	 * Color labels. Missing color interpreted as white.
	 */
	private static final String COLOR = "COLOR";

	/**
	 * BFS will not further traverse neighbors of any node in the stopSet.
	 */
	private Set<String> stopSet;

	public BFS(SIFGraph graph, EdgeSelector selector, Set<String> sourceSet, Set<String> stopSet, Direction direction,
		int limit)
	{
		super(graph, selector, sourceSet, direction, limit);
		this.stopSet = stopSet;
	}

	public Map<Object, Integer> run()
	{
		// Initialize label, maps and queue

		dist = new HashMap<>();
		LinkedList<String> queue = new LinkedList<>();


		// Initialize dist and color of source set

		for (String source : sourceNodes)
		{
			setLabel(source, 0);
			setColor(source, Color.GRAY);
		}

		// Add all source nodes to the queue if traversal is needed

		if (limit > 0)
		{
			queue.addAll(sourceNodes);
		}

		// Process the queue

		while (!queue.isEmpty())
		{
			String current = queue.remove(0);

			// Process edges towards the direction

			for (SIFEdge edge : direction == Direction.DOWNSTREAM ? graph.getDownstream(current) :
				direction == Direction.UPSTREAM ? graph.getUpstream(current) : graph.getAllEdges(current))
			{
				if (!edgeSelector.select(edge)) continue;

				// Label the edge considering direction of traversal and type of current node

				if (direction == Direction.UPSTREAM)
				{
					setLabel(edge, getLabel(current) + 1);
				}
				else
				{
					setLabel(edge, getLabel(current));
				}

				// Get the other end of the edge
				String neigh = direction == Direction.DOWNSTREAM ? edge.getTarget() :
					direction == Direction.UPSTREAM ? edge.getSource() : edge.getOtherEnd(current);

				// Process the neighbor if not processed or not in queue

				if (getColor(neigh) == Color.WHITE)
				{
					// Label the neighbor according to the search direction and node type

					if (direction == Direction.UPSTREAM)
					{
						setLabel(neigh, getLabel(edge));
					}
					else
					{
						setLabel(neigh, getLabel(current) + 1);
					}

					// Check if we need to stop traversing the neighbor, enqueue otherwise

					if ((stopSet == null || !stopSet.contains(neigh)) && getLabel(neigh) < limit)
					{
						setColor(neigh, Color.GRAY);

						// Enqueue the node according to its type

						queue.addLast(neigh);
					}
					else
					{
						// If we do not want to traverse this neighbor, we paint it black
						setColor(neigh, Color.BLACK);
					}
				}
			}

			// Current node is processed
			setColor(current, Color.BLACK);
		}

		return dist;
	}

	private Color getColor(String node)
	{
		if (!hasLabel(node, COLOR))
		{
			// Absence of color is interpreted as white
			return Color.WHITE;
		}
		else
		{
			return Color.valueOf(getLabel(node, COLOR));
		}
	}

	private void setColor(String node, Color color)
	{
		putLabel(node, COLOR, color.val);
	}

	public int getLabel(Object go)
	{
		if (!dist.containsKey(go))
		{
			// Absence of label is interpreted as infinite
			return Integer.MAX_VALUE-(limit*2);
		}
		else
		{
			return dist.get(go);
		}
	}

	private void setLabel(Object go, int label)
	{
		dist.put(go, label);
	}

	enum Color
	{
		WHITE(0),
		GRAY(1),
		BLACK(2);

		int val;

		Color(int val)
		{
			this.val = val;
		}

		static Color valueOf(int val)
		{
			for (Color color : values())
			{
				if (color.val == val) return color;
			}
			throw new IllegalArgumentException("Color value not known: " + val);
		}
	}
}
