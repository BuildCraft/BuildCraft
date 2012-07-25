package net.minecraft.src.buildcraft.core;

import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.blueprints.BptSlotInfo;

public interface IBptContributor {

	public void saveToBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

	public void loadFromBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

}
