/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;

import buildcraft.core.builders.BuilderItemMetaPair;
import buildcraft.core.builders.BuildingSlotBlock;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.lib.fluids.Tank;

public class BuildingSlotMapIterator {
	public static int MAX_PER_ITEM = 512;
	private final BptBuilderBlueprint builderBlueprint;
	private final Map<BuilderItemMetaPair, List<BuildingSlotBlock>> slotMap;
	private final Set<BuilderItemMetaPair> availablePairs = new HashSet<BuilderItemMetaPair>();
	private final int[] buildStageOccurences;
	private final boolean isCreative;
	private Iterator<BuilderItemMetaPair> keyIterator;
	private BuilderItemMetaPair currentKey;
	private List<BuildingSlotBlock> slots;
	private int slotPos, slotFound;

	public BuildingSlotMapIterator(BptBuilderBlueprint builderBlueprint, TileAbstractBuilder builder) {
		this.builderBlueprint = builderBlueprint;
		this.slotMap = builderBlueprint.buildList;
		this.buildStageOccurences = builderBlueprint.buildStageOccurences;
		this.isCreative = builder == null
				|| builder.getWorldObj().getWorldInfo().getGameType() == WorldSettings.GameType.CREATIVE;

		reset();
	}

	public void refresh(TileAbstractBuilder builder) {
		if (!isCreative) {
			availablePairs.clear();
			availablePairs.add(new BuilderItemMetaPair(null));

			if (builder != null) {
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
		}
	}

	public void skipKey() {
		findNextKey();
	}

	private void findNextKey() {
		slotPos = -1;
		slotFound = 0;
		slots = null;
		while (keyIterator.hasNext()) {
			currentKey = keyIterator.next();
			if (isCreative || availablePairs.contains(currentKey)) {
				slots = slotMap.get(currentKey);
				slotPos = currentKey.position - 1;
				return;
			}
		}
		this.currentKey = null;
		this.keyIterator = slotMap.keySet().iterator();
	}

	public BuildingSlotBlock next() {
		if (slots == null) {
			findNextKey();
		}
		while (slots != null) {
			slotPos++;
			while (slotFound < MAX_PER_ITEM && slotPos < slots.size()) {
				BuildingSlotBlock b = slots.get(slotPos);
				if (b != null) {
					slotFound++;
					currentKey.position = slotPos + 1;
					return b;
				}
				slotPos++;
			}
			if (slotFound >= MAX_PER_ITEM) {
				currentKey.position = slotPos;
			} else if (slotPos >= slots.size()) {
				currentKey.position = 0;
			}
			findNextKey();
		}
		return null;
	}

	public void remove() {
		BuildingSlotBlock slot = slots.get(slotPos);
		slots.set(slotPos, null);

		builderBlueprint.onRemoveBuildingSlotBlock(slot);
	}

	public void reset() {
		this.keyIterator = slotMap.keySet().iterator();
		this.currentKey = null;
		this.slots = null;
		findNextKey();
	}
}
