/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.ArrayList;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidStack;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.api.core.Position;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRefinery extends BlockContainer {

	private static Icon icon;

	public BlockRefinery(int i) {
		super(i, Material.iron);

		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileRefinery();
	}

	@Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving, ItemStack stack) {
		super.onBlockPlacedBy(world, i, j, k, entityliving, stack);

		ForgeDirection orientation = Utils.get2dOrientation(new Position(entityliving.posX, entityliving.posY, entityliving.posZ), new Position(i, j, k));

		world.setBlockMetadataWithNotify(i, j, k, orientation.getOpposite().ordinal(), 1);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

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
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			world.markBlockForUpdate(i, j, k);
			return true;
		} else {

			LiquidStack liquid = LiquidContainerRegistry.getLiquidForFilledItem(entityplayer.getCurrentEquippedItem());

			if (liquid != null) {
				int qty = ((TileRefinery) world.getBlockTileEntity(i, j, k)).fill(ForgeDirection.UNKNOWN, liquid, true);

				if (qty != 0 && !BuildCraftCore.debugMode && !entityplayer.capabilities.isCreativeMode) {
					entityplayer.inventory.setInventorySlotContents(entityplayer.inventory.currentItem,
							Utils.consumeItem(entityplayer.inventory.getCurrentItem()));
				}

				return true;
			}
		}

		if (!CoreProxy.proxy.isRenderWorld(world)) {
			entityplayer.openGui(BuildCraftFactory.instance, GuiIds.REFINERY, world, i, j, k);
		}

		return true;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		icon = par1IconRegister.registerIcon("buildcraft:refineryBack");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		return icon;
	}
}
