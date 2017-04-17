package org.pathwaycommons.sif.query;

import org.pathwaycommons.sif.io.Writer;
import org.pathwaycommons.sif.model.RelationType;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFEdge;
import org.pathwaycommons.sif.model.SIFGraph;

import java.io.IOException;
import java.io.PrintStream;

/**
 * @author Ozgun Babur
 */
public class Mocker
{
	/**
	 *        <- 7 ->
	 *       3       4
	 *       |   1   |
	 *       |  > \  |
	 *       v /   > v
	 *  9 <- S       T <- 10 -> 11
	 *       | <   / |
	 *       |  \ <  |
	 *       v   2   v
	 *       5       6
	 *        -> 8 <-
	 */
	public static SIFGraph mockGraph1(RelationType type)
	{
		SIFGraph graph = new SIFGraph();
		graph.add(new SIFEdge("S", "1", type));
		graph.add(new SIFEdge("1", "T", type));
		graph.add(new SIFEdge("2", "S", type));
		graph.add(new SIFEdge("T", "2", type));
		graph.add(new SIFEdge("3", "S", type));
		graph.add(new SIFEdge("4", "T", type));
		graph.add(new SIFEdge("S", "5", type));
		graph.add(new SIFEdge("T", "6", type));
		graph.add(new SIFEdge("7", "3", type));
		graph.add(new SIFEdge("7", "4", type));
		graph.add(new SIFEdge("5", "8", type));
		graph.add(new SIFEdge("6", "8", type));
		graph.add(new SIFEdge("S", "9", type));
		graph.add(new SIFEdge("10", "T", type));
		graph.add(new SIFEdge("10", "11", type));
		return graph;
	}

	public static void main(String[] args) throws IOException
	{
		SIFGraph graph = mockGraph1(RelationTypeEnum.CONTROLS_STATE_CHANGE_OF);
		Writer writer = new Writer(false);
		writer.write(graph, System.out);
	}
}
