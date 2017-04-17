package org.pathwaycommons.sif.util;

import org.junit.Test;
import org.pathwaycommons.sif.model.RelationType;
import org.pathwaycommons.sif.model.RelationTypeEnum;
import org.pathwaycommons.sif.model.SIFEdge;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.*;

/**
 * @author Ozgun Babur
 */
public class RelationTypeSelectorTest
{

	@Test
	public void testSelect() throws Exception
	{
		RelationType myType = new RelationType()
		{
			@Override
			public String getName()
			{
				return "custom-type";
			}

			@Override
			public boolean isDirected()
			{
				return false;
			}

			@Override
			public int hashCode()
			{
				return getName().hashCode() + Boolean.valueOf(isDirected()).hashCode();
			}

			@Override
			public boolean equals(Object obj)
			{
				return obj instanceof RelationType &&
					((RelationType) obj).getName().equals(getName()) &&
					((RelationType) obj).isDirected() == isDirected();
			}
		};

		RelationTypeSelector selector = new RelationTypeSelector(new HashSet<>(Arrays.asList(
			RelationTypeEnum.CONTROLS_EXPRESSION_OF, RelationTypeEnum.CONTROLS_PRODUCTION_OF, myType)));

		assertEquals(true, selector.select(new SIFEdge("S", "T", RelationTypeEnum.CONTROLS_PRODUCTION_OF)));
		assertEquals(true, selector.select(new SIFEdge("S", "T", new RelationType()
		{
			@Override
			public String getName()
			{
				return "custom-type";
			}

			@Override
			public boolean isDirected()
			{
				return false;
			}

			@Override
			public int hashCode()
			{
				return getName().hashCode() + Boolean.valueOf(isDirected()).hashCode();
			}

			@Override
			public boolean equals(Object obj)
			{
				return obj instanceof RelationType &&
					((RelationType) obj).getName().equals(getName()) &&
					((RelationType) obj).isDirected() == isDirected();
			}
		})));

		assertEquals(false, selector.select(new SIFEdge("S", "T", RelationTypeEnum.CONTROLS_STATE_CHANGE_OF)));
	}
}