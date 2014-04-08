package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public interface ICustomHighlight{

	public AxisAlignedBB[] getBoxes(World wrd, int x, int y, int z, EntityPlayer player);

	public double getExpansion();
}
