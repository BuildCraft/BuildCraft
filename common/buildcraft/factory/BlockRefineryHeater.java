package buildcraft.factory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.core.CreativeTabBuildCraft;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockRefineryHeater extends Block{
	
	IIcon textureTop;
	IIcon textureSide;

	public BlockRefineryHeater(){
		super(Material.iron);
		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.TIER_2.get());
	}
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return true;
	}

	public boolean isACube() {
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureTop = par1IconRegister.registerIcon("buildcraft:refinery_heater_top");
		textureSide = par1IconRegister.registerIcon("buildcraft:refinery_heater_sides_active");
	}

	@Override
	public IIcon getIcon(int i, int j) {
		switch (i) {
			case 0:
				return textureTop;
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

}
