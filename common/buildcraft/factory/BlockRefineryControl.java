package buildcraft.factory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.utils.MultiBlockCheck;
import buildcraft.core.utils.Utils;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockRefineryControl extends BlockContainer{
	
	IIcon textureTop;
	IIcon textureFront;
	IIcon textureSide;
	
	public BlockRefineryControl(){
		super(Material.iron);
		setHardness(5F);
		setCreativeTab(CreativeTabBuildCraft.TIER_2.get());
	}

	@Override
	public TileEntity createNewTileEntity(World world, int var) {
		return new TileRefineryControl();
	}
	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack){
		ForgeDirection orientation = Utils.get2dOrientation(entityliving);

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ){
		TileEntity tile = world.getTileEntity(x, y, z);

		if (!(tile instanceof TileRefineryControl))
			return false;
		// Drop through if the player is sneaking
				if (player.isSneaking()) {
					return false;
				}

				if (player.getCurrentEquippedItem() != null) {
					if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
						return false;
					}
				}

				if (!world.isRemote) {
					if (buildcraft.core.utils.MultiBlockCheck.isPartOfAMultiBlock("refinery", x, y, z, world)){
						player.addStat(BuildCraftCore.refineAndRedefineAchievement, 1);
					}
					player.openGui(BuildCraftFactory.instance, GuiIds.REFINERY_CONTROL, world, x, y, z);
				}

				return true;
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureSide = par1IconRegister.registerIcon("buildcraft:refinery_controller_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:refinery_controller_side");
		textureFront = par1IconRegister.registerIcon("buildcraft:refinery_controller_front");
	}
	
	@Override
	public IIcon getIcon(int i, int j) {
		// If no metadata is set, then this is an icon.
		if (j == 0 && i == 3) {
			return textureFront;
		}

		if (i == j && i > 1) {
			return textureFront;
		}

		switch (i) {
			case 1:
				return textureTop;
			default:
				return textureSide;
		}
	}

}
