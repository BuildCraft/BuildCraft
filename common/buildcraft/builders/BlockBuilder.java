/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class BlockBuilder extends BlockContainer {

	Icon blockTextureTop;
	Icon blockTextureSide;
	Icon blockTextureFront;

	public BlockBuilder(int i) {
		super(i, Material.iron);
		setHardness(5F);
		setCreativeTab(null);
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileBuilder();
	}

	@Override
	public Icon getIcon(int i, int j) {
		if (j == 0 && i == 3)
			return blockTextureFront;

		if (i == j)
			return blockTextureFront;

		switch (i) {
			case 1:
				return blockTextureTop;
			default:
				return blockTextureSide;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			int meta = world.getBlockMetadata(i, j, k);

			switch (ForgeDirection.values()[meta]) {
				case WEST:
					world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.SOUTH.ordinal(), 0);
					break;
				case EAST:
					world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.NORTH.ordinal(), 0);
					break;
				case NORTH:
					world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.WEST.ordinal(), 0);
					break;
				case SOUTH:
				default:
					world.setBlockMetadataWithNotify(i, j, k, ForgeDirection.EAST.ordinal(), 0);
					break;
			}

			world.markBlockForUpdate(i, j, k);
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;
		} else {

			if (!CoreProxy.proxy.isRenderWorld(world)) {
				entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.BUILDER, world, i, j, k);
			}
			return true;

		}
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLivingBase entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);
		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		blockTextureTop = par1IconRegister.registerIcon("buildcraft:builder_top");
		blockTextureSide = par1IconRegister.registerIcon("buildcraft:builder_side");
		blockTextureFront = par1IconRegister.registerIcon("buildcraft:builder_front");
	}
}
