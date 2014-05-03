/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.ItemBuildCraft;

public class ItemPlug extends ItemBuildCraft {

	public ItemPlug() {
		super();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.PipePlug";
	}

//	@Override
//	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World worldObj, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {
//		if (worldObj.isRemote)
//			return false;
//		TileEntity tile = worldObj.getTileEntity(x, y, z);
//		if (!(tile instanceof TileGenericPipe))
//			return false;
//		TileGenericPipe pipeTile = (TileGenericPipe) tile;
//
//		if (player.isSneaking()) { // Strip plug
//			if (!pipeTile.hasPlug(ForgeDirection.VALID_DIRECTIONS[side]))
//				return false;
//			pipeTile.removeAndDropPlug(ForgeDirection.VALID_DIRECTIONS[side]);
//			return true;
//		} else {
//			if (((TileGenericPipe) tile).addPlug(ForgeDirection.VALID_DIRECTIONS[side])){
//				if (!player.capabilities.isCreativeMode) {
//					stack.stackSize--;
//				}
//				return true;
//			}
//			return false;
//		}
//	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player) {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
	    // NOOP
	}

	@Override
    @SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
        return 0;
    }

}
