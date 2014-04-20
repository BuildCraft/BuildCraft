package buildcraft.factory;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import buildcraft.BuildCraftFactory;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;

public class BlockCanner extends BlockBuildCraft{
	
	IIcon textureFront;
	IIcon textureTop;
	IIcon textureSide;

	public BlockCanner() {
		super(Material.iron, CreativeTabBuildCraft.TIER_3);
		setHardness(5F);
		setResistance(10F);
		}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileCanner();
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, x, y, z, entityplayer, par6, par7, par8, par9);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (entityplayer.getCurrentEquippedItem() != null) {
			if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftFactory.instance, GuiIds.CANNER, world, x, y, z);
		}

		return true;
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
		case 0:
			return textureTop;
		case 1:
			return textureTop;
		default:
			return textureSide;
		}
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		textureFront = par1IconRegister.registerIcon("buildcraft:canner_front");
		textureSide = par1IconRegister.registerIcon("buildcraft:canner_side");
		textureTop = par1IconRegister.registerIcon("buildcraft:canner_top");
	}
}
