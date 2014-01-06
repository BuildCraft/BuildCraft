package buildcraft.core;

import buildcraft.factory.TileRefinery;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPingPong extends BlockBuildCraft {

	public static final Random rand = new Random();

	public BlockPingPong(int id) {
		super(id, Material.rock);
		setBlockUnbreakable();
		setResistance(6000000.0F);
		setStepSound(soundStoneFootstep);
		disableStats();
		setTickRandomly(true);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TilePingPong();
	}
}
