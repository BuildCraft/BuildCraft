/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.science;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftCore;
import buildcraft.silicon.ItemRedstoneChipset;

public enum Tier {
	WoodenGear,
	StoneGear,
	IronGear,
	GoldenGear,
	DiamondGear,
	EmeraldGear,
	RedstoneCrystalGear,

	Chipset,
	IronChipset,
	GoldenChipset,
	DiamondChipset,
	EmeraldChipset,
	RedstoneCrystalChipset,
	Unrevealed;

	private static ItemStack[] stacksToDisplay;

	private static TechnoTier[] technologies;

	public ItemStack getStackToDisplay () {
		if (stacksToDisplay == null) {
			stacksToDisplay = new ItemStack[Tier.values().length];

			stacksToDisplay[WoodenGear.ordinal()] = new ItemStack(BuildCraftCore.woodenGearItem);
			stacksToDisplay[StoneGear.ordinal()] = new ItemStack(BuildCraftCore.stoneGearItem);
			stacksToDisplay[IronGear.ordinal()] = new ItemStack(BuildCraftCore.ironGearItem);
			stacksToDisplay[GoldenGear.ordinal()] = new ItemStack(BuildCraftCore.goldGearItem);
			stacksToDisplay[DiamondGear.ordinal()] = new ItemStack(BuildCraftCore.diamondGearItem);
			stacksToDisplay[EmeraldGear.ordinal()] = new ItemStack(Blocks.bedrock);
			stacksToDisplay[RedstoneCrystalGear.ordinal()] = new ItemStack(Blocks.bedrock);
			stacksToDisplay[Chipset.ordinal()] = ItemRedstoneChipset.Chipset.RED.getStack();
			stacksToDisplay[IronChipset.ordinal()] = ItemRedstoneChipset.Chipset.IRON.getStack();
			stacksToDisplay[GoldenChipset.ordinal()] = ItemRedstoneChipset.Chipset.GOLD.getStack();
			stacksToDisplay[DiamondChipset.ordinal()] = ItemRedstoneChipset.Chipset.DIAMOND.getStack();
			stacksToDisplay[EmeraldChipset.ordinal()] = ItemRedstoneChipset.Chipset.EMERALD.getStack();
			stacksToDisplay[RedstoneCrystalChipset.ordinal()] = new ItemStack(Blocks.bedrock);
			stacksToDisplay[Unrevealed.ordinal()] = new ItemStack(Blocks.bedrock);
		}

		return stacksToDisplay[ordinal()];
	}

	public Technology getTechnology() {
		return technologies[ordinal()];
	}

	public static void initializeTechnologies() {
		technologies[WoodenGear.ordinal()].initialize(
				WoodenGear);
		technologies[StoneGear.ordinal()].initialize(
				StoneGear,
				new ItemStack(BuildCraftCore.woodenGearItem, 50));
		technologies[IronGear.ordinal()].initialize(
				IronGear,
				new ItemStack(BuildCraftCore.woodenGearItem, 75),
				new ItemStack(BuildCraftCore.stoneGearItem, 50));
		technologies[GoldenGear.ordinal()].initialize(
				GoldenGear,
				new ItemStack(BuildCraftCore.woodenGearItem, 125),
				new ItemStack(BuildCraftCore.stoneGearItem, 75),
				new ItemStack(BuildCraftCore.ironGearItem, 10));
		technologies[DiamondGear.ordinal()].initialize(
				DiamondGear,
				new ItemStack(BuildCraftCore.stoneGearItem, 125),
				new ItemStack(BuildCraftCore.ironGearItem, 15),
				new ItemStack(BuildCraftCore.goldGearItem, 5));
		technologies[EmeraldGear.ordinal()].initialize(
				EmeraldGear,
				new ItemStack(BuildCraftCore.stoneGearItem, 200));
		technologies[RedstoneCrystalGear.ordinal()].initialize(
				RedstoneCrystalGear);
		technologies[Chipset.ordinal()].initialize(
				Chipset);
		technologies[IronChipset.ordinal()].initialize(
				IronChipset);
		technologies[GoldenChipset.ordinal()].initialize(
				GoldenChipset);
		technologies[DiamondChipset.ordinal()].initialize(
				DiamondChipset);
		technologies[EmeraldChipset.ordinal()].initialize(
				EmeraldChipset);
		technologies[RedstoneCrystalChipset.ordinal()].initialize(
				RedstoneCrystalChipset);
		technologies[Unrevealed.ordinal()].initialize(
				Unrevealed);
	}

	static {
		technologies = new TechnoTier[Tier.values().length];

		for (int i = 0; i < technologies.length; ++i) {
			technologies[i] = new TechnoTier();
		}
	}
}
