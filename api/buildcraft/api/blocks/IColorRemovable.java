package buildcraft.api.blocks;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public interface IColorRemovable {
	boolean removeColorFromBlock(World world, int x, int y, int z, ForgeDirection side);
}
