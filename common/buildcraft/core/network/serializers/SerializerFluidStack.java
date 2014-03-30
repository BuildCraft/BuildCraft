/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.network.serializers;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.core.utils.Utils;

public class SerializerFluidStack extends ClassSerializer {

	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		FluidStack stack = (FluidStack) o;

		if (stack == null) {
			data.writeBoolean(false);
		} else {
			data.writeShort(stack.getFluid().getID());
			data.writeInt(stack.amount);

			if (stack.tag == null) {
				data.writeBoolean(false);
			} else {
				data.writeBoolean(true);
				Utils.writeNBT(data, stack.tag);
			}
		}

	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		if (!data.readBoolean()) {
			return null;
		} else {
			int id = data.readShort();
			int amount = data.readerIndex();

			NBTTagCompound nbt = null;

			if (data.readBoolean()) {
				nbt = Utils.readNBT(data);
				return new FluidStack(id, amount, nbt);
			} else {
				return new FluidStack(id, amount);
			}
		}
	}
}
