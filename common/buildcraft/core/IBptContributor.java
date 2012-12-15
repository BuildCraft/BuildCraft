package buildcraft.core;

import net.minecraft.tileentity.TileEntity;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.core.blueprints.BptBase;

public interface IBptContributor {

	public void saveToBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

	public void loadFromBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

}
