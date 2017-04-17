package org.pathwaycommons.sif.model;

import org.jgrapht.graph.DefaultEdge;
import org.pathwaycommons.sif.util.EdgeAnnotationType;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ozgun Babur
 */
public class SIFEdge extends DefaultEdge
{
	/**
	 * Source node.
	 */
	String source;

	/**
	 * Target node.
	 */
	String target;

	/**
	 * The type of SIF relation.
	 */
	private RelationType type;

	private Map<EdgeAnnotationType, String> annotationMap;

	public SIFEdge(String source, String target, RelationType type)
	{
		this.source = source;
		this.target = target;
		this.type = type;
	}

	@Override
	public String getSource()
	{
		return source;
	}

	@Override
	public String getTarget()
	{
		return target;
	}

	public String getOtherEnd(String oneEnd)
	{
		if (source.equals(oneEnd)) return target;
		if (target.equals(oneEnd)) return source;
		throw new IllegalArgumentException(
			"The given end is not part of this relation. Given: " + oneEnd + ", this: " + this);
	}

	public RelationType getType()
	{
		return type;
	}

	public boolean isDirected()
	{
		return type.isDirected();
	}

	public void addAnnotation(EdgeAnnotationType type, String info)
	{
		if (this.annotationMap == null) annotationMap = new HashMap<>();
		annotationMap.put(type, info);
	}

	public String getAnnotation(EdgeAnnotationType type)
	{
		if (annotationMap == null) return null;
		return annotationMap.get(type);
	}

	public SIFEdge getReverse()
	{
		SIFEdge edge = new SIFEdge(target, source, type);
		edge.annotationMap = annotationMap;
		return edge;
	}

	@Override
	public int hashCode()
	{
		return source.hashCode() + target.hashCode() + type.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this) return true;

		if (obj instanceof SIFEdge)
		{
			SIFEdge e = (SIFEdge) obj;
			return e.type.equals(type) &&
				((e.source.equals(source) && e.target.equals(target)) ||
				(!isDirected() && e.source.equals(target) && e.target.equals(source)));
		}

		//else
		return false;
	}

	@Override
	public String toString()
	{
		return source + " " + type.getName() + " " + target;
	}
}
