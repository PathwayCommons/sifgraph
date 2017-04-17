package org.pathwaycommons.sif.util;

/**
 * First 3 columns are fixed in a SIF file, and not represented in this enum.
 *
 * @author Ozgun Babur
 */
public enum EdgeAnnotationType
{
	MEDIATORS,
	PUBMED_IDS,
	PMC_IDS,
	PATHWAY_NAMES,
	LOCATIONS,
	SITES,
	DATA_SOURCE,
}
