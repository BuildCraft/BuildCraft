package buildcraft.core;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.World;


public abstract class BlockBuildCraft extends BlockContainer {

	protected static boolean keepInventory = false;
	protected Random rand;

	protected BlockBuildCraft(int id, Material material) {
		super(id, material);
		this.rand = new Random();
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}
 
	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

}
