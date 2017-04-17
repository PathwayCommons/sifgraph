package org.pathwaycommons.sif.io;

import org.pathwaycommons.sif.model.SIFEdge;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.util.EdgeAnnotationType;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ozgun Babur
 */
public class Writer
{
	EdgeAnnotationType[] edgeAnnots;

	boolean writeNodeAnnotations;

	public Writer(boolean writeNodeAnnotations, EdgeAnnotationType... edgeAnnots)
	{
		this.edgeAnnots = edgeAnnots;
		this.writeNodeAnnotations = writeNodeAnnotations;
	}

	public void write(SIFGraph graph, OutputStream out) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writeEdges(graph.getAllEdges(), writer);

		if (writeNodeAnnotations)
		{
			for (String node : graph.getAllNodes())
			{
				String nodeAnnotation = graph.getNodeAnnotation(node);
				if (nodeAnnotation != null)
				{
					writer.write("\n" + nodeAnnotation);
				}
			}
		}

		writer.close();
	}

	public void write(Set<Object> queryResult, OutputStream out) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
		writeEdges(
			queryResult.stream().filter(o -> o instanceof SIFEdge).map(o -> (SIFEdge) o).collect(Collectors.toSet()),
			writer);
		writer.close();
	}

	private void writeEdges(Set<SIFEdge> graphObjects, BufferedWriter writer) throws IOException
	{
		for (SIFEdge edge : graphObjects)
		{
			writer.write(edge.getSource() + "\t" + edge.getType().getName() + "\t" + edge.getTarget());

			for (EdgeAnnotationType annType : edgeAnnots)
			{
				String s = edge.getAnnotation(annType);
				if (s == null) s = "";
				writer.write("\t" + s);
			}

			writer.write("\n");
		}
	}
}
