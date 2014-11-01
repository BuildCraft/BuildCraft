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
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.core.utils.Utils;

/**
 * An NBT-based serializer for IStatementParameters.
 */
public class SerializerIStatementParameter extends ClassSerializer {

	@Override
	public void write (ByteBuf data, Object o, SerializationContext context) {
		IStatementParameter parameter = (IStatementParameter) o;

		if (parameter == null) {
			data.writeBoolean(false);
		} else {
			NBTTagCompound cpt = new NBTTagCompound();
			parameter.writeToNBT(cpt);
			
			data.writeBoolean(true);
			Utils.writeUTF(data, parameter.getUniqueTag());
			Utils.writeNBT(data, cpt);
		}
	}

	@Override
	public Object read (ByteBuf data, Object o, SerializationContext context) {
		if (!data.readBoolean()) {
			return null;
		} else {
			String kind = Utils.readUTF(data);
			IStatementParameter parameter = StatementManager.createParameter(kind);
			parameter.readFromNBT(Utils.readNBT(data));
			return parameter;
		}
	}
}
