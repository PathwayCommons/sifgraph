package org.pathwaycommons.sif.model;

/**
 * @author Ozgun Babur
 */
public enum SignedTypeEnum implements RelationType
{
	PHOSPHORYLATES(true),
	DEPHOSPHORYLATES(true),
	UPREGULATES_EXPRESSION(true),
	DOWNREGULATES_EXPRESSION(true),
	;

	private boolean directed;

	SignedTypeEnum(boolean directed)
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

	public static SignedTypeEnum toEnum(String name)
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
