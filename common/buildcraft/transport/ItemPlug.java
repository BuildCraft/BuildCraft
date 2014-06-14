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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.transport.IPipePluggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.ItemBuildCraft;

public class ItemPlug extends ItemBuildCraft {

	public ItemPlug() {
		super();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.PipePlug";
	}

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

	public static class PlugPluggable implements IPipePluggable {
		public PlugPluggable() {

		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {

		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {

		}

		@Override
		public ItemStack[] getDropItems(IPipeTile pipe) {
			return new ItemStack[] { new ItemStack(BuildCraftTransport.plugItem) };
		}

		@Override
		public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {

		}

		@Override
		public void onDetachedPipe(IPipeTile pipe, ForgeDirection direction) {

		}

		@Override
		public boolean blocking(IPipeTile pipe, ForgeDirection direction) {
			return true;
		}
	}
}
