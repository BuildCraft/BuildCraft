package buildcraft.core;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class BlockSpring extends Block {

	public static final Random rand = new Random();

	public enum EnumSpring {

		WATER(5, -1, Block.waterStill),
		OIL(6000, 8, null); // Set in BuildCraftEnergy
		public static final EnumSpring[] VALUES = values();
		public final int tickRate, chance;
		public Block liquidBlock;
		public boolean canGen = true;

		private EnumSpring(int tickRate, int chance, Block liquidBlock) {
			this.tickRate = tickRate;
			this.chance = chance;
			this.liquidBlock = liquidBlock;
		}

		public static EnumSpring fromMeta(int meta) {
			if (meta < 0 || meta >= VALUES.length) {
				return WATER;
			}
			return VALUES[meta];
		}
	}

	public BlockSpring(int id) {
		super(id, Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		disableStats();
		setTickRandomly(true);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
	}

	@Override
	public void getSubBlocks(int id, CreativeTabs tab, List list) {
		for (EnumSpring type : EnumSpring.VALUES) {
			list.add(new ItemStack(this, 1, type.ordinal()));
		}
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		assertSpring(world, x, y, z);
	}

//	@Override
//	public void onNeighborBlockChange(World world, int x, int y, int z, int blockid) {
//		assertSpring(world, x, y, z);
//	}
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		world.scheduleBlockUpdate(x, y, z, blockID, EnumSpring.fromMeta(meta).tickRate);
	}

	private void assertSpring(World world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		EnumSpring spring = EnumSpring.fromMeta(meta);
		world.scheduleBlockUpdate(x, y, z, blockID, spring.tickRate);
		if (!spring.canGen || spring.liquidBlock == null) {
			return;
		}
		if (!world.isAirBlock(x, y + 1, z)) {
			return;
		}
		if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
			return;
		}
		world.setBlock(x, y + 1, z, spring.liquidBlock.blockID);
	}

	// Prevents updates on chunk generation
	@Override
	public boolean func_82506_l() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		blockIcon = par1IconRegister.registerIcon("bedrock");
	}
}
