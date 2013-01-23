package buildcraft.core;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

public class BlockSpring extends Block {

	public BlockSpring(int id) {
		super(id, 17, Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		disableStats();
		setTickRandomly(true);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		assertSpring(world, x, y, z);
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int blockid) {
		assertSpring(world, x, y, z);
	}
	
	private void assertSpring(World world, int x, int y, int z) {
		
		if(!world.isAirBlock(x, y + 1, z))
			return;
		
		world.setBlockWithNotify(x, y + 1, z, Block.waterStill.blockID);
	}
}
