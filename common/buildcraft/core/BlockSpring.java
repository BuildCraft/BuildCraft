package buildcraft.core;

import java.util.Random;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.world.World;

public class BlockSpring extends Block {

	public BlockSpring(int id) {
		super(id, Material.rock);
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

		world.func_94575_c(x, y + 1, z, Block.waterStill.blockID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void func_94332_a(IconRegister par1IconRegister)
	{
	    field_94336_cN = par1IconRegister.func_94245_a("water");
	}
}
