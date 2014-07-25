/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import net.minecraft.item.ItemStack;

public class TechnoTier extends Technology {

	private ItemStack itemToDisplay;
	private Technology[] prerequisites;
	private ItemStack[] requirements;

	public TechnoTier(Tier iTier,
			Object requirement,
			Technology... iPrerequisites) {
		this(iTier, requirement, null, null, iPrerequisites);
	}

	public TechnoTier(Tier iTier,
			Object requirement1,
			Object requirement2,
			Technology... iPrerequisites) {
		this(iTier, requirement1, requirement2, null, iPrerequisites);
	}

	public TechnoTier(Tier iTier,
			Object requirement1,
			Object requirement2,
			Object requirement3,
			Technology... iPrerequisites) {

		super(Tier.values()[(iTier.ordinal() > 0 ? iTier.ordinal() - 1 : 0)],
				requirement1,
				requirement2,
				requirement3);

		itemToDisplay = toStack(iTier.getStackToDisplay());

		prerequisites = iPrerequisites;
	}

	@Override
	public ItemStack getStackToDisplay() {
		return itemToDisplay;
	}

	@Override
	public String getLocalizedName() {
		return "";
	}
}
