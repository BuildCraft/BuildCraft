/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

public class PipePowerHeat extends Pipe<PipeTransportPower> {

	int zeroAcc = 0;
	double powerLevel = 0;

	SafeTimeTracker scanTracker = new SafeTimeTracker(40, 5);

	public PipePowerHeat(int itemID) {
		super(new PipeTransportPower(), itemID);
		transport.initFromPipe(getClass());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	public int getHeatLevel () {
		if (powerLevel == 0) {
			return 0;
		} else if (powerLevel >= 1000) {
			return 8;
		} else {
			return 1 + (int) (powerLevel / 1000F * 7F);
		}
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container == null) {
			return PipeIconProvider.TYPE.PipePowerHeat0.ordinal();
		} else {
			return PipeIconProvider.TYPE.PipePowerHeat0.ordinal()
					+ container.worldObj.getBlockMetadata(container.xCoord,
							container.yCoord, container.zCoord);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!CoreProxy.proxy.isSimulating(container.worldObj)) {
			return;
		}

		PipeTransportPower power = ((PipeTransportPower) transport);

		power.requestEnergy(ForgeDirection.UP, 1024);

		powerLevel = power.clearInstantPower ();

		int meta = container.worldObj.getBlockMetadata(container.xCoord, container.yCoord, container.zCoord);
		int newMeta = getHeatLevel();

		if (meta != newMeta) {
			System.out.println ("SEND NEW META " + newMeta);
			container.worldObj.setBlockMetadataWithNotify(container.xCoord,
					container.yCoord, container.zCoord, newMeta, 2);
			container.scheduleRenderUpdate();
		}

		if (powerLevel >= 10 && scanTracker.markTimeIfDelay(container.worldObj)) {
			int x = container.xCoord;
			int y = container.yCoord;
			int z = container.zCoord;

			for (int xi = x - 1; xi <= x + 1; ++xi) {
				for (int yi = y - 1; yi <= y + 1; ++yi) {
					for (int zi = z - 1; zi <= z + 1; ++zi) {
						if (container.worldObj.getBlockId(xi, yi, zi) == Block.blockRedstone.blockID) {
							container.worldObj.setBlock(xi, yi, zi, 0);

							for (int i = 0; i < 4; ++i) {
								ItemStack stack = new ItemStack(
										BuildCraftCore.redstoneCrystal);
								EntityItem entityitem = new EntityItem(
										container.worldObj, xi + 0.5F,
										yi + 0.5F, zi + 0.5F, stack);

								entityitem.lifespan = BuildCraftCore.itemLifespan;
								entityitem.delayBeforeCanPickup = 10;

								float f3 = 0.05F;
								entityitem.motionX = (float) (container.worldObj.rand
										.nextGaussian() - 0.5F) * f3;
								entityitem.motionY = (float) (container.worldObj.rand
										.nextGaussian() - 0.5F) * f3;
								entityitem.motionZ = (float) (container.worldObj.rand
										.nextGaussian() - 0.5F) * f3;
								container.worldObj
										.spawnEntityInWorld(entityitem);
							}
						}

					}
				}
			}
		}
	}
}
