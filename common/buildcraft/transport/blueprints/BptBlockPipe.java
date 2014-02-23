/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.blueprints;

import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftTransport;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.Pipe;

public class BptBlockPipe extends BptBlock {

	public BptBlockPipe(Block block) {
		super(block);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		Item pipeItem = context.getMappingRegistry().getItemForId (slot.cpt.getInteger("pipeId"));

		requirements.add(new ItemStack(pipeItem));

		if (slot.cpt.hasKey("wire0")) {
			requirements.add(new ItemStack(BuildCraftTransport.pipeWire, 1, 0));
		}

		if (slot.cpt.hasKey("wire1")) {
			requirements.add(new ItemStack(BuildCraftTransport.pipeWire, 1, 1));
		}

		if (slot.cpt.hasKey("wire2")) {
			requirements.add(new ItemStack(BuildCraftTransport.pipeWire, 1, 2));
		}

		if (slot.cpt.hasKey("wire3")) {
			requirements.add(new ItemStack(BuildCraftTransport.pipeWire, 1, 3));
		}

		/*if (slot.cpt.hasKey("gate")) {
			int gateId = slot.cpt.getInteger("gate");
			if (slot.cpt.hasKey("hasPulser") && slot.cpt.getBoolean("hasPulser")) {
				requirements.add(new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, gateId - 1));
			} else {
				requirements.add(new ItemStack(BuildCraftTransport.pipeGate, 1, gateId - 1));
			}
		}

		if (BuildCraftCore.itemBptProps[pipeId] != null) {
			BuildCraftCore.itemBptProps[pipeId].addRequirements(slot, requirements);
		}*/
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), slot.x, slot.y, slot.z);

		if (BlockGenericPipe.isValid(pipe)) {
			return pipe.item == Item.itemRegistry.getObjectById(slot.cpt
					.getInteger("pipeId"));
		} else {
			return false;
		}
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		/*if (BuildCraftCore.itemBptProps[pipeId] != null) {
			BuildCraftCore.itemBptProps[pipeId].rotateLeft(slot, context);
		}*/
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		Pipe pipe = BlockGenericPipe.createPipe(context.getMappingRegistry()
				.getItemForId(pipeId));

		for (int i = 0; i < pipe.wireSet.length; ++i) {
			if (slot.cpt.hasKey("wire" + i)) {
				pipe.wireSet[i] = true;
			}
		}

		/*if (slot.cpt.hasKey("gate")) {
			int gateId = slot.cpt.getInteger("gate");
			GateVanilla newGate;
			if (slot.cpt.hasKey("hasPulser") && slot.cpt.getBoolean("hasPulser")) {
				newGate = new GateVanilla(pipe, new ItemStack(BuildCraftTransport.pipeGateAutarchic, 1, gateId - 1));
			} else {
				newGate = new GateVanilla(pipe, new ItemStack(BuildCraftTransport.pipeGate, 1, gateId - 1));
			}
			pipe.gate = newGate;

			for (int i = 0; i < 8; ++i) {
				if (slot.cpt.hasKey("trigger" + i)) {
					pipe.gate.actions[i] = ActionManager.triggers[slot.cpt.getInteger("trigger" + i)];
				}

				if (slot.cpt.hasKey("triggerParameter" + i)) {
					ItemStack s = ItemStack.loadItemStackFromNBT((NBTTagCompound) slot.cpt.getTag("triggerParameter" + i));

					if (s != null) {
						pipe.triggerParameters[i] = new TriggerParameter();
						pipe.triggerParameters[i].set(s);
					}
				}

				if (slot.cpt.hasKey("action" + i)) {
					pipe.activatedActions[i] = ActionManager.actions[slot.cpt.getInteger("action" + i)];
				}
			}
		}*/

		BlockGenericPipe.placePipe(pipe, context.world(), slot.x, slot.y,
				slot.z, slot.block, slot.meta);

		/*if (BuildCraftCore.itemBptProps[pipeId] != null) {
			BuildCraftCore.itemBptProps[pipeId].buildBlock(slot, context);
		}*/
	}

	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			context.getMappingRegistry().setIdForItem(pipe.item, Item.getIdFromItem(pipe.item));
			bptSlot.cpt.setInteger("pipeId", context.getMappingRegistry()
					.getIdForItem(pipe.item));

			for (int i = 0; i < pipe.wireSet.length; ++i)
				if (pipe.wireSet[i]) {
					bptSlot.cpt.setInteger("wire" + i, 1);
				}

			// / TODO: Does not save/load custom gates
			/*if (pipe.hasGate()) {
				bptSlot.cpt.setInteger("gate", pipe.gate.kind.ordinal());
				if (pipe.gate instanceof GateVanilla) {
					bptSlot.cpt.setBoolean("hasPulser", ((GateVanilla) pipe.gate).hasPulser());
				}

				for (int i = 0; i < 8; ++i) {
					if (pipe.activatedTriggers[i] != null) {
						bptSlot.cpt.setInteger("trigger" + i, pipe.activatedTriggers[i].getId());
					}

					if (pipe.triggerParameters[i] != null) {
						NBTTagCompound subCpt = new NBTTagCompound();
						pipe.triggerParameters[i].getItemStack().writeToNBT(subCpt);

						bptSlot.cpt.setTag("triggerParameter" + i, subCpt);
					}

					if (pipe.activatedActions[i] != null) {
						bptSlot.cpt.setInteger("action" + i, pipe.activatedActions[i].getId());
					}
				}
			}

			if (BuildCraftCore.itemBptProps[pipe.itemID] != null) {
				BuildCraftCore.itemBptProps[pipe.itemID].initializeFromWorld(bptSlot, context, x, y, z);
			}*/
		}
	}

	@Override
	public void postProcessing(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		//if (BuildCraftCore.itemBptProps[pipeId] != null) {
		//	BuildCraftCore.itemBptProps[pipeId].postProcessing(slot, context);
		//}
	}

}
