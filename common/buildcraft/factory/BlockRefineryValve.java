package buildcraft.factory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.core.CreativeTabBuildCraft;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockRefineryValve extends BlockContainer{
	
	IIcon icon;

	public BlockRefineryValve(){
		super(Material.iron);
		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.TIER_2.get());
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileRefineryValve();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:refinery_valve");
	}
	
	@Override
	public IIcon getIcon(int i, int j) {
		return icon;
	}

}
