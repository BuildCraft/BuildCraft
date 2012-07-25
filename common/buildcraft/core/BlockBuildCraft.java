package buildcraft.core;

import java.util.Random;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public abstract class BlockBuildCraft extends BlockContainer implements ITextureProvider {

	protected static boolean keepInventory = false;
	protected Random rand;

	protected BlockBuildCraft(int id, Material material) {
		super(id, material);
		this.rand = new Random();
	}

	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);
		super.onBlockRemoval(world, i, j, k);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

}
