package org.pathwaycommons.sif.util;

import org.pathwaycommons.sif.model.SIFEdge;

/**
 * @author Ozgun Babur
 */
public interface EdgeSelector
{
	boolean select(SIFEdge edge);
}
