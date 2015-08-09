/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import buildcraft.core.lib.fluids.Tank;

public class BuildingSlotMapIterator {
	private static final int MAX_PER_ITEM = 64;
	private final Map<BuilderItemMetaPair, List<BuildingSlotBlock>> slots;
	private final Set<BuilderItemMetaPair> availablePairs = new HashSet<BuilderItemMetaPair>();
	private final int[] buildStageOccurences;
	private final boolean isCreative;
	private Iterator<BuilderItemMetaPair> impIterator;
	private BuilderItemMetaPair pair;
	private List<BuildingSlotBlock> current;
	private int position, returnsThisCurrent;

	public BuildingSlotMapIterator(Map<BuilderItemMetaPair, List<BuildingSlotBlock>> slots, TileAbstractBuilder builder,
								   int[] buildStageOccurences) {
		this.slots = slots;
		this.impIterator = slots.keySet().iterator();
		this.buildStageOccurences = buildStageOccurences;
		this.isCreative = builder == null
				|| builder.getWorldObj().getWorldInfo().getGameType() == WorldSettings.GameType.CREATIVE;

		// Generate available pairs
		if (builder != null) {
			availablePairs.add(new BuilderItemMetaPair(null));
			for (int i = 0; i < builder.getSizeInventory(); i++) {
				ItemStack stack = builder.getStackInSlot(i);
				if (stack != null) {
					availablePairs.add(new BuilderItemMetaPair(stack));
				}
			}
			for (Tank t : builder.getFluidTanks()) {
				if (t.getFluid() != null && t.getFluid().getFluid().getBlock() != null) {
					availablePairs.add(new BuilderItemMetaPair(new ItemStack(t.getFluid().getFluid().getBlock())));
				}
			}
		}

		findNewCurrent();
	}

	public void skipList() {
		findNewCurrent();
	}

	private void findNewCurrent() {
		position = -1;
		returnsThisCurrent = 0;
		while (impIterator.hasNext()) {
			pair = impIterator.next();
			if (isCreative || availablePairs.contains(pair)) {
				current = slots.get(pair);
				position = pair.position - 1;
				return;
			}
		}
		current = null;
	}

	public BuildingSlotBlock next() {
		while (current != null) {
			position++;
			while (returnsThisCurrent < MAX_PER_ITEM && position < current.size()) {
				BuildingSlotBlock b = current.get(position);
				if (b != null) {
					returnsThisCurrent++;
					return b;
				}
				position++;
			}
			if (returnsThisCurrent >= MAX_PER_ITEM) {
				pair.position = position;
			} else if (position >= current.size()) {
				pair.position = 0;
			}
			findNewCurrent();
		}
		return null;
	}

	public void remove() {
		buildStageOccurences[current.get(position).buildStage]--;
		current.set(position, null);
	}

	public void reset() {
		this.impIterator = slots.keySet().iterator();
		this.pair = null;
		this.current = null;
		findNewCurrent();
	}
}
