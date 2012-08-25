/**
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.LinkedList;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.blueprints.BlockSignature;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.TriggerParameter;
import buildcraft.core.DefaultProps;

import net.minecraft.src.Block;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class BptBlockPipe extends BptBlock {

	public BptBlockPipe(int blockId) {
		super(blockId);
	}

	@Override
	public void addRequirements(BptSlotInfo slot, IBptContext context, LinkedList<ItemStack> requirements) {
		int pipeId = slot.cpt.getInteger("pipeId");

		requirements.add(new ItemStack(pipeId, 1, 0));

		if (slot.cpt.hasKey("wire0"))
			requirements.add(new ItemStack(BuildCraftTransport.redPipeWire));

		if (slot.cpt.hasKey("wire1"))
			requirements.add(new ItemStack(BuildCraftTransport.bluePipeWire));

		if (slot.cpt.hasKey("wire2"))
			requirements.add(new ItemStack(BuildCraftTransport.greenPipeWire));

		if (slot.cpt.hasKey("wire3"))
			requirements.add(new ItemStack(BuildCraftTransport.yellowPipeWire));

		if (slot.cpt.hasKey("gate")) {
			int gateId = slot.cpt.getInteger("gate");
			requirements.add(new ItemStack(BuildCraftTransport.pipeGate, 1, gateId - 1));
		}

		if (BuildCraftCore.itemBptProps[pipeId] != null)
			BuildCraftCore.itemBptProps[pipeId].addRequirements(slot, requirements);
	}

	@Override
	public boolean isValid(BptSlotInfo slot, IBptContext context) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), slot.x, slot.y, slot.z);

		if (BlockGenericPipe.isValid(pipe))
			return pipe.itemID == slot.cpt.getInteger("pipeId");
		else
			return false;
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		if (BuildCraftCore.itemBptProps[pipeId] != null)
			BuildCraftCore.itemBptProps[pipeId].rotateLeft(slot, context);
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		Pipe pipe = BlockGenericPipe.createPipe(pipeId);

		for (int i = 0; i < pipe.wireSet.length; ++i)
			if (slot.cpt.hasKey("wire" + i))
				pipe.wireSet[i] = true;

		if (slot.cpt.hasKey("gate")) {
			// / TODO: Does not save/load custom gates
			int gateId = slot.cpt.getInteger("gate");
			pipe.gate = new GateVanilla(pipe);
			pipe.gate.kind = Gate.GateKind.values()[gateId];

			for (int i = 0; i < 8; ++i) {
				if (slot.cpt.hasKey("trigger" + i))
					pipe.activatedTriggers[i] = ActionManager.triggers[slot.cpt.getInteger("trigger" + i)];

				if (slot.cpt.hasKey("triggerParameter" + i)) {
					ItemStack s = ItemStack.loadItemStackFromNBT((NBTTagCompound) slot.cpt.getTag("triggerParameter" + i));

					if (s != null) {
						pipe.triggerParameters[i] = new TriggerParameter();
						pipe.triggerParameters[i].set(s);
					}
				}

				if (slot.cpt.hasKey("action" + i))
					pipe.activatedActions[i] = ActionManager.actions[slot.cpt.getInteger("action" + i)];
			}
		}

		BlockGenericPipe.placePipe(pipe, context.world(), slot.x, slot.y, slot.z, slot.blockId, slot.meta);

		if (BuildCraftCore.itemBptProps[pipeId] != null)
			BuildCraftCore.itemBptProps[pipeId].buildBlock(slot, context);
	}

	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		Pipe pipe = BlockGenericPipe.getPipe(context.world(), x, y, z);

		if (BlockGenericPipe.isValid(pipe)) {
			bptSlot.cpt.setInteger("pipeId", pipe.itemID);

			for (int i = 0; i < pipe.wireSet.length; ++i)
				if (pipe.wireSet[i])
					bptSlot.cpt.setInteger("wire" + i, 1);

			// / TODO: Does not save/load custom gates
			if (pipe.hasGate()) {
				bptSlot.cpt.setInteger("gate", pipe.gate.kind.ordinal());

				for (int i = 0; i < 8; ++i) {
					if (pipe.activatedTriggers[i] != null)
						bptSlot.cpt.setInteger("trigger" + i, pipe.activatedTriggers[i].getId());

					if (pipe.triggerParameters[i] != null) {
						NBTTagCompound subCpt = new NBTTagCompound();
						pipe.triggerParameters[i].getItemStack().writeToNBT(subCpt);

						bptSlot.cpt.setTag("triggerParameter" + i, subCpt);
					}

					if (pipe.activatedActions[i] != null)
						bptSlot.cpt.setInteger("action" + i, pipe.activatedActions[i].getId());
				}
			}

			if (BuildCraftCore.itemBptProps[pipe.itemID] != null)
				BuildCraftCore.itemBptProps[pipe.itemID].initializeFromWorld(bptSlot, context, x, y, z);
		}
	}

	@Override
	public void postProcessing(BptSlotInfo slot, IBptContext context) {
		int pipeId = slot.cpt.getInteger("pipeId");

		if (BuildCraftCore.itemBptProps[pipeId] != null)
			BuildCraftCore.itemBptProps[pipeId].postProcessing(slot, context);
	}

	@Override
	public BlockSignature getSignature(Block block) {
		BlockSignature sig = super.getSignature(block);

		sig.mod = "BuildCraftTransport";
		sig.modVersion = DefaultProps.VERSION;

		return sig;
	}

}
