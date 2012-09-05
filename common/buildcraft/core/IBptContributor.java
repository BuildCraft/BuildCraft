package buildcraft.core;

import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.core.blueprints.BptBase;
import net.minecraft.src.TileEntity;

public interface IBptContributor {

	public void saveToBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

	public void loadFromBluePrint(TileEntity builder, BptBase bluePrint, BptSlotInfo slot);

}
