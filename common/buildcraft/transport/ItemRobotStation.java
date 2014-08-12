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
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.RobotRegistry;

public class ItemRobotStation extends ItemBuildCraft {

	public ItemRobotStation() {
		super(CreativeTabBuildCraft.ITEMS);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item.PipeRobotStation";
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

	public static class RobotStationPluggable implements IPipePluggable {
		private DockingStation station;
		private boolean isValid = false;

		public RobotStationPluggable() {

		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {

		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {

		}

		@Override
		public ItemStack[] getDropItems(IPipeTile pipe) {
			return new ItemStack[] { new ItemStack(BuildCraftTransport.robotStationItem) };
		}

		@Override
		public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {
			validate(pipe, direction);
		}

		@Override
		public void onDetachedPipe(IPipeTile pipe, ForgeDirection direction) {
			invalidate();
		}

		public DockingStation getStation() {
			return station;
		}

		@Override
		public boolean blocking(IPipeTile pipe, ForgeDirection direction) {
			return true;
		}

		@Override
		public void invalidate() {
			if (station != null && !station.getPipe().getWorld().isRemote) {
				RobotRegistry.getRegistry(station.world).removeStation(station);
				isValid = false;
			}
		}

		@Override
		public void validate(IPipeTile pipe, ForgeDirection direction) {
			TileGenericPipe gPipe = (TileGenericPipe) pipe;
			if (!isValid && !gPipe.getWorld().isRemote) {
				station = RobotRegistry.getRegistry(gPipe.getWorld()).getStation(
						gPipe.xCoord,
						gPipe.yCoord,
						gPipe.zCoord,
						direction);

				if (station == null) {
					station = new DockingStation(gPipe, direction);
					RobotRegistry.getRegistry(gPipe.getWorld()).registerStation(station);
				}

				isValid = true;
			}
		}
	}
}
