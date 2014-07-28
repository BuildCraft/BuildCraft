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
import net.minecraft.util.IIcon;

import buildcraft.api.gates.IStatement;

public class TechnoStatement extends Technology {

	private IStatement statementToDisplay;
	private String unlocalizedName;

	public void initialize(Tier iTier,
			IStatement statementToDisplay,
			String unlocalizedName,
			ItemStack requirement,
			Technology... iPrerequisites) {
		initialize(iTier, statementToDisplay, unlocalizedName, requirement, null, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			IStatement statementToDisplay,
			String unlocalizedName,
			ItemStack requirement1,
			ItemStack requirement2,
			Technology... iPrerequisites) {
		initialize(iTier, statementToDisplay, unlocalizedName, requirement1, requirement2, null, iPrerequisites);
	}

	public void initialize(Tier iTier,
			IStatement iStatementToDisplay,
			String unlocalizedName,
			ItemStack requirement1,
			ItemStack requirement2,
			ItemStack requirement3,
			Technology... iPrerequisites) {

		super.initialize("statement:" + iStatementToDisplay.getUniqueTag(),
				iTier, requirement1, requirement2, requirement3, iPrerequisites);

		statementToDisplay = iStatementToDisplay;
	}

	@Override
	public IIcon getIcon() {
		return statementToDisplay.getIcon();
	}

	@Override
	public String getLocalizedName() {
		return unlocalizedName;
	}
}
