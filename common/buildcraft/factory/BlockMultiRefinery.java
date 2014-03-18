package buildcraft.factory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftFactory;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockMultiRefinery extends Block {
	private static IIcon icon;
	
	public BlockMultiRefinery(){
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
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack){
		if (buildcraft.core.utils.MultiBlockCheck.isPartOfAMultiBlock("refinery", i, j, k, world)){
			System.out.println("ok");
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:refineryBack");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2) {
		return icon;
	}
}
