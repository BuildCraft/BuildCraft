/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import buildcraft.api.blueprints.BlockSignature;
import buildcraft.api.blueprints.BptBlock;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import buildcraft.api.core.Orientations;
import buildcraft.core.DefaultProps;
import net.minecraft.src.Block;

public class BptBlockEngine extends BptBlock {

	public BptBlockEngine(int blockId) {
		super(blockId);
	}

	@Override
	public void rotateLeft(BptSlotInfo slot, IBptContext context) {
		int o = slot.cpt.getInteger("orientation");

		o = Orientations.values()[o].rotateLeft().ordinal();

		slot.cpt.setInteger("orientation", o);
	}

	@Override
	public void initializeFromWorld(BptSlotInfo bptSlot, IBptContext context, int x, int y, int z) {
		TileEngine engine = (TileEngine) context.world().getBlockTileEntity(x, y, z);

		bptSlot.cpt.setInteger("orientation", engine.engine.orientation.ordinal());
	}

	@Override
	public void buildBlock(BptSlotInfo slot, IBptContext context) {
		context.world().setBlockAndMetadataWithNotify(slot.x, slot.y, slot.z, slot.blockId, slot.meta);

		TileEngine engine = (TileEngine) context.world().getBlockTileEntity(slot.x, slot.y, slot.z);

		engine.orientation = slot.cpt.getInteger("orientation");
	}

	@Override
	public BlockSignature getSignature(Block block) {
		BlockSignature sig = super.getSignature(block);

		sig.mod = "BuildCraftEnergy";
		sig.modVersion = DefaultProps.VERSION;

		return sig;
	}
}
