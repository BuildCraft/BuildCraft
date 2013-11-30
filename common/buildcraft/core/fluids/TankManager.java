/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.fluids;

import com.google.common.collect.ForwardingList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class TankManager<T extends Tank> extends ForwardingList<T> implements IFluidHandler, List<T> {

	private List<T> tanks = new ArrayList<T>();

	public TankManager() {
	}

	public TankManager(T... tanks) {
		addAll(Arrays.asList(tanks));
	}

	@Override
	protected List<T> delegate() {
		return tanks;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		for (Tank tank : tanks) {
			int used = tank.fill(resource, doFill);
			if (used > 0)
				return used;
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		if (resource == null)
			return null;
		for (Tank tank : tanks) {
			if (!resource.isFluidEqual(tank.getFluid()))
				continue;
			FluidStack drained = tank.drain(resource.amount, doDrain);
			if (drained != null && drained.amount > 0)
				return drained;
		}
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		for (Tank tank : tanks) {
			FluidStack drained = tank.drain(maxDrain, doDrain);
			if (drained != null && drained.amount > 0)
				return drained;
		}
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		FluidTankInfo[] info = new FluidTankInfo[size()];
		for (int i = 0; i < size(); i++) {
			info[i] = get(i).getInfo();
		}
		return info;
	}

	public void writeToNBT(NBTTagCompound data) {
		for (Tank tank : tanks) {
			tank.writeToNBT(data);
		}
	}

	public void readFromNBT(NBTTagCompound data) {
		for (Tank tank : tanks) {
			tank.readFromNBT(data);
		}
	}

	public void writeData(DataOutputStream data) throws IOException {
		for (Tank tank : tanks) {
			FluidStack fluidStack = tank.getFluid();
			if (fluidStack != null && fluidStack.getFluid() != null) {
				data.writeShort(fluidStack.getFluid().getID());
				data.writeInt(fluidStack.amount);
				data.writeBoolean(fluidStack.tag != null);
				if(fluidStack.tag != null) {
					NBTBase.writeNamedTag(fluidStack.tag, data);
				}
			} else {
				data.writeShort(-1);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void readData(DataInputStream data) throws IOException {
		for (Tank tank : tanks) {
			int fluidId = data.readShort();
			if (fluidId > 0) {
				FluidStack fluidStack = new FluidStack(fluidId, data.readInt());
				if(data.readBoolean()) {
					fluidStack.tag = (NBTTagCompound) NBTBase.readNamedTag(data);
				}
				tank.setFluid(fluidStack);
			} else {
				tank.setFluid(null);
			}
		}
	}
}
