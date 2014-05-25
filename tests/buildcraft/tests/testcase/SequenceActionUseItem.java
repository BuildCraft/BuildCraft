/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests.testcase;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import buildcraft.api.core.NetworkData;
import buildcraft.core.proxy.CoreProxy;

public class SequenceActionUseItem extends SequenceAction {

	@NetworkData
	ItemStack stack;

	@NetworkData
	int x, y, z, face;

	public SequenceActionUseItem() {

	}

	public SequenceActionUseItem(World iWorld, ItemStack iStack, int ix, int iy, int iz, int iface) {
		stack = iStack;
		x = ix;
		y = iy;
		z = iz;
		face = iface;
		world = iWorld;
		date = world.getTotalWorldTime();
	}

	@Override
	public void execute() {
		stack.getItem().onItemUse(stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world).get(), world, x, y,
				z, face, x, y, z);
		System.out.println("[TEST " + date + "] [USE ITEM] " + x + ", " + y + ", " + z + ": " + stack.toString());
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);
		nbt.setInteger("face", face);

		NBTTagCompound stackNBT = new NBTTagCompound();
		stack.writeToNBT(stackNBT);
		nbt.setTag("stack", stackNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		x = nbt.getInteger("x");
		y = nbt.getInteger("y");
		z = nbt.getInteger("z");
		face = nbt.getInteger("face");
		stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
	}

}
