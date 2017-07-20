/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.particle;

import buildcraft.lib.BCLibProxy;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiConsumer;

public class MessageParticleVanilla implements IMessage {
    public EnumParticleTypes type;
    public double x, y, z;
    public double dx, dy, dz;
    public boolean ignoreRange;
    public int[] parameters;

    @Override
    public void fromBytes(ByteBuf buf) {
        type = EnumParticleTypes.getParticleFromId(buf.readUnsignedShort());
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
        dx = buf.readDouble();
        dy = buf.readDouble();
        dz = buf.readDouble();
        ignoreRange = buf.readBoolean();
        parameters = new int[type.getArgumentCount()];
        for (int i = 0; i < parameters.length; i++) {
            parameters[i] = buf.readInt();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(type.getParticleID());
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(dx);
        buf.writeDouble(dy);
        buf.writeDouble(dz);
        buf.writeBoolean(ignoreRange);
        for (int i = 0; i < type.getArgumentCount(); i++) {
            if (parameters == null || parameters.length <= i) {
                buf.writeInt(0);
            } else {
                buf.writeInt(parameters[i]);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void spawn(World world) {
        world.spawnParticle(type, ignoreRange, x, y, z, dx, dy, dz, parameters);
    }

    private static final BiConsumer<MessageParticleVanilla, MessageContext> HANDLER_CLIENT = (message, ctx) ->
            message.spawn(BCLibProxy.getProxy().getClientWorld());

    public static final IMessageHandler<MessageParticleVanilla, IMessage> HANDLER = (message, ctx) -> {
        HANDLER_CLIENT.accept(message, ctx);
        return null;
    };
}
