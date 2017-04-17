package org.pathwaycommons.sif.model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ozgun Babur
 */
public class SIFGraph
{
	Map<String, Set<SIFEdge>> incoming;
	Map<String, Set<SIFEdge>> outgoing;

	Map<String, String> extendedSIFNodeAnnotationMap;

	public SIFGraph()
	{
		incoming = new HashMap<>();
		outgoing = new HashMap<>();
	}

	public void add(SIFEdge edge)
	{
		addAsIfDirected(edge);

		if (!edge.isDirected())
		{
			SIFEdge rev = edge.getReverse();
			addAsIfDirected(rev);
		}
	}

	public void remove(SIFEdge edge)
	{
		removeAsIfDirected(edge);

		if (!edge.isDirected())
		{
			SIFEdge rev = edge.getReverse();
			removeAsIfDirected(rev);
		}
	}

	private void addAsIfDirected(SIFEdge edge)
	{
		if (!incoming.containsKey(edge.target)) incoming.put(edge.target, new HashSet<>());
		if (!outgoing.containsKey(edge.source)) outgoing.put(edge.source, new HashSet<>());

		incoming.get(edge.target).add(edge);
		outgoing.get(edge.source).add(edge);
	}

	private void removeAsIfDirected(SIFEdge edge)
	{
		if (incoming.containsKey(edge.target)) incoming.get(edge.target).remove(edge);
		if (outgoing.containsKey(edge.source)) outgoing.get(edge.source).remove(edge);

		if (incoming.get(edge.target).isEmpty()) incoming.remove(edge.target);
		if (outgoing.get(edge.source).isEmpty()) outgoing.remove(edge.source);
	}

	public Set<SIFEdge> getUpstream(String node)
	{
		if (incoming.containsKey(node)) return incoming.get(node);
		return Collections.emptySet();
	}

	public Set<SIFEdge> getDownstream(String node)
	{
		if (outgoing.containsKey(node)) return outgoing.get(node);
		return Collections.emptySet();
	}

	public Set<SIFEdge> getAllEdges(String node)
	{
		return Stream.concat(getUpstream(node).stream(), getDownstream(node).stream()).collect(Collectors.toSet());
	}

	public Set<SIFEdge> getAllEdges()
	{
		return Stream.concat(incoming.values().stream(), outgoing.values().stream())
			.flatMap(Collection::stream).collect(Collectors.toSet());
	}

	public Set<String> getAllNodes()
	{
		return Stream.concat(incoming.keySet().stream(), outgoing.keySet().stream()).collect(Collectors.toSet());
	}

	public Set<SIFEdge> getUpstream(Set<String> nodes)
	{
		Set<SIFEdge> result = new HashSet<SIFEdge>();
		for (String node : nodes)
		{
			result.addAll(getUpstream(node));
		}
		return result;
	}

	public Set<SIFEdge> getDownstream(Set<String> nodes)
	{
		Set<SIFEdge> result = new HashSet<SIFEdge>();
		for (String node : nodes)
		{
			result.addAll(getDownstream(node));
		}
		return result;
	}

	public Set<String> getUpstreamNodes(String node)
	{
		return collectSources(incoming.get(node));
	}

	public Set<String> getDownstreamNodes(String node)
	{
		return collectTargets(outgoing.get(node));
	}

	public Set<String> getUpstreamNodes(Set<String> nodes)
	{
		return collectSources(getUpstream(nodes));
	}

	public Set<String> getDownstreamNodes(Set<String> nodes)
	{
		return collectTargets(getDownstream(nodes));
	}

	private Set<String> collectTargets(Set<SIFEdge> edges)
	{
		return edges.stream().map(SIFEdge::getTarget).collect(Collectors.toSet());
	}

	private Set<String> collectSources(Set<SIFEdge> edges)
	{
		return edges.stream().map(SIFEdge::getSource).collect(Collectors.toSet());
	}

	public String getNodeAnnotation(String node)
	{
		if (extendedSIFNodeAnnotationMap != null)
		{
			return extendedSIFNodeAnnotationMap.get(node);
		}
		return null;
	}

	public void addNodeAnnotation(String node, String wholeLine)
	{
		if (extendedSIFNodeAnnotationMap == null) extendedSIFNodeAnnotationMap = new HashMap<>();
		extendedSIFNodeAnnotationMap.put(node, wholeLine);
	}
}
