package org.pathwaycommons.sif.util;

import org.pathwaycommons.sif.model.RelationType;
import org.pathwaycommons.sif.model.SIFEdge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class RelationTypeSelector implements EdgeSelector
{
	Set<RelationType> types;

	public RelationTypeSelector(Set<RelationType> types)
	{
		this.types = types;
	}

	public RelationTypeSelector(RelationType... types)
	{
		this.types = new HashSet<>(Arrays.asList(types));
	}

	@Override
	public boolean select(SIFEdge edge)
	{
		return types.contains(edge.getType());
	}
}
