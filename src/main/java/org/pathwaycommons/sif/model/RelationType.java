package org.pathwaycommons.sif.model;

/**
 * Type of the SIF relation.
 */
public interface RelationType
{
	/**
	 * Name of the relation. Style is hypenated small letters, such like "controls-state-change-of".
	 */
	String getName();

	/**
	 * True if this relation have a direction.
	 */
	boolean isDirected();
}
