package buildcraft.energy;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockFlywheel extends BlockBuildCraft {
	public static IIcon FW_SIDE, FW_TOP;

	public BlockFlywheel() {
		super(Material.iron);
		setRotatable(true);
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.875F, 1.0F);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileFlywheel();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		super.registerBlockIcons(register);
		FW_SIDE = register.registerIcon("buildcraftenergy:flywheelBlock/fwside");
		FW_TOP = register.registerIcon("buildcraftenergy:flywheelBlock/fwtop");
	}
}
