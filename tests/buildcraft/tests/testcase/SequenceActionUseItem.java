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

import buildcraft.core.proxy.CoreProxy;

public class SequenceActionUseItem extends SequenceAction {

	ItemStack stack;
	int x, y, z;

	public SequenceActionUseItem() {

	}

	public SequenceActionUseItem(World iWorld, ItemStack iStack, int ix, int iy, int iz) {
		stack = iStack;
		x = ix;
		y = iy;
		z = iz;
		world = iWorld;
		date = world.getTotalWorldTime();
	}

	@Override
	public void execute() {
		stack.getItem().onItemUse(stack, CoreProxy.proxy.getBuildCraftPlayer((WorldServer) world).get(), world, x, y,
				z, 1, x, y, z);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setInteger("x", x);
		nbt.setInteger("y", y);
		nbt.setInteger("z", z);

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
		stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));
	}

}
