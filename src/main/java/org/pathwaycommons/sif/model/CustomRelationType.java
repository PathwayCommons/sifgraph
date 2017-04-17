package org.pathwaycommons.sif.model;

/**
 * @author Ozgun Babur
 */
public class CustomRelationType implements RelationType
{
	private String name;
	private boolean directed;

	public CustomRelationType(String name, boolean directed)
	{
		this.name = name;
		this.directed = directed;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public boolean isDirected()
	{
		return directed;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof CustomRelationType && ((CustomRelationType) obj).getName().equals(getName())
			&& ((CustomRelationType) obj).isDirected() == isDirected();
	}

	@Override
	public int hashCode()
	{
		return getName().hashCode() + Boolean.valueOf(directed).hashCode();
	}
}
