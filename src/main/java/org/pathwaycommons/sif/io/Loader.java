package org.pathwaycommons.sif.io;

import org.pathwaycommons.sif.model.*;
import org.pathwaycommons.sif.util.EdgeAnnotationType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class Loader
{
	EdgeAnnotationType[] types;

	public Loader(EdgeAnnotationType... types)
	{
		this.types = types;
	}

	public SIFGraph load(InputStream is) throws IOException
	{
		Map<String, RelationType> relTypeMap = new HashMap<>();
		Map<String, String> annotationStorage = new HashMap<>();

		SIFGraph graph = new SIFGraph();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));

		String line = reader.readLine();
		while (line != null && !line.isEmpty())
		{
			String[] token = line.split("\t");

			if (token.length < 3)
			{
				System.err.println("Invalid SIF file. Column size = " + token.length + ".\nLine = \"" + line + "\"");
				return null;
			}

			RelationType type = relTypeMap.get(token[1]);

			if (type == null)
			{
				type = RelationTypeEnum.toEnum(token[1]);
				if (type == null)
				{
					type = SignedTypeEnum.toEnum(token[1]);
				}
				if (type == null)
				{
					type = new CustomRelationType(token[1], true);
				}
				relTypeMap.put(token[1], type);
			}
			SIFEdge edge = new SIFEdge(token[0], token[2], type);

			for (int i = 3; i < Math.min(token.length, types.length + 3); i++)
			{
				if (!token[i].isEmpty())
				{
					String annot = token[i];

					// check if this String is already in memory, if yes, do not store duplicate
					if (annotationStorage.containsKey(annot))
					{
						annot = annotationStorage.get(annot);
					}
					else
					{
						annotationStorage.put(annot, annot);
					}

					edge.addAnnotation(types[i - 3], annot);
				}
			}

			graph.add(edge);

			line = reader.readLine();
		}

		if (line != null && line.isEmpty())
		{
			line = reader.readLine();
			while (line != null)
			{
				String node = line.substring(0, line.indexOf("\t"));
				graph.addNodeAnnotation(node, line);
				line = reader.readLine();
			}
		}

		reader.close();
		return graph;
	}
}
