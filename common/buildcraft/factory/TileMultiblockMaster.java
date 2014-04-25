package buildcraft.factory;

import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import net.minecraft.entity.player.EntityPlayer;

public class TileMultiblockMaster extends TileBuildCraft {

	@NetworkData
	public boolean formed = false;

	public void onBlockActivated(EntityPlayer player) {

	}

	public void formMultiblock(EntityPlayer player) {
		formed = true;
	}

	public void deformMultiblock() {
		formed = false;
	}

}
