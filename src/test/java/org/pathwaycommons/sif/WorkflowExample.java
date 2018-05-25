package org.pathwaycommons.sif;

import org.pathwaycommons.sif.io.Loader;
import org.pathwaycommons.sif.io.Writer;
import org.pathwaycommons.sif.model.SIFGraph;
import org.pathwaycommons.sif.query.QueryExecutor;
import org.pathwaycommons.sif.util.EdgeAnnotationType;
import org.pathwaycommons.sif.util.EdgeSelector;
import org.pathwaycommons.sif.util.RelationTypeSelector;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import static org.pathwaycommons.sif.model.RelationTypeEnum.*;
import static org.pathwaycommons.sif.util.EdgeAnnotationType.*;

/**
 * Just a snippet of code showing how to use this library on Pathway Commons SIF.
 *
 * @author Ozgun Babur
 */
public class WorkflowExample
{
	public static void main(String[] args) throws IOException
	{
		//--- Load the graph

		// Tell the type and order of edge annotations in the SIF resource
		EdgeAnnotationType[] edgeAnnotTypes = new EdgeAnnotationType[]
		{
			DATA_SOURCE, PUBMED_IDS, PATHWAY_NAMES, MEDIATORS
		};

		// Initialize loader
		Loader loader = new Loader(edgeAnnotTypes);

		// Load the NCI PID graph
		SIFGraph graph = loader.load(new GZIPInputStream( new URL(
			"http://www.pathwaycommons.org/archives/PC2/v10/PathwayCommons10.All.hgnc.txt.gz")
				.openStream()));

		//--- Perform a query on the graph

		// The query will traverse only two type of relations in this example
		EdgeSelector edgeSelector = new RelationTypeSelector(CONTROLS_STATE_CHANGE_OF, CONTROLS_EXPRESSION_OF);

		// Select a seed
		Set<String> seed = new HashSet<>(Arrays.asList("EGF", "CDKN2A"));

		// Run the query
		Set<Object> result = QueryExecutor.searchPathsBetween(graph, edgeSelector, seed, true, 2);

		//--- Report results

		// Initialize the writer with the same edge annotation style
		Writer writer = new Writer(false, edgeAnnotTypes);

		// Write results
		writer.write(result, System.out);
	}
}
