package org.pathwaycommons.sif.model;

/**
 * @author Ozgun Babur
 */
public enum RelationTypeEnum implements RelationType
{
	CONTROLS_STATE_CHANGE_OF(true),
	CONTROLS_PHOSPHORYLATION_OF(true),
	CONTROLS_TRANSPORT_OF(true),
	CONTROLS_EXPRESSION_OF(true),
	IN_COMPLEX_WITH(false),
	CATALYSIS_PRECEDES(true),
	INTERACTS_WITH(false),
	NEIGHBOR_OF(false),
	CONSUMPTION_CONTROLLED_BY(true),
	CONTROLS_PRODUCTION_OF(true),
	CONTROLS_TRANSPORT_OF_CHEMICAL(true),
	CHEMICAL_AFFECTS(true),
	REACTS_WITH(false),
	USED_TO_PRODUCE(true),
	;

	private boolean directed;

	RelationTypeEnum(boolean directed)
	{
		this.directed = directed;
	}

	@Override
	public String getName()
	{
		return name().toLowerCase().replaceAll("_", "-");
	}

	@Override
	public boolean isDirected()
	{
		return directed;
	}

	public static RelationTypeEnum toEnum(String name)
	{
		name = name.toUpperCase().replaceAll("-", "_");

		try
		{
			return valueOf(name);
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
