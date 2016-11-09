package buildcraft.lib.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCMessageHandler;

import io.netty.buffer.ByteBuf;

public class MessageParticleVanilla implements IMessage {
    private static final boolean DEBUG = BCMessageHandler.DEBUG;

    public EnumParticleTypes type;
    public double x, y, z;
    public double dx, dy, dz;
    public boolean ignoreRange;
    public int[] paramaters;

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
        paramaters = new int[type.getArgumentCount()];
        for (int i = 0; i < paramaters.length; i++) {
            paramaters[i] = buf.readInt();
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
            if (paramaters == null || paramaters.length <= i) {
                buf.writeInt(0);
            } else {
                buf.writeInt(paramaters[i]);
            }
        }
    }

    private void spawn(World world) {
        world.spawnParticle(type, ignoreRange, x, y, z, dx, dy, dz, paramaters);
    }

    public enum Handler implements IMessageHandler<MessageParticleVanilla, IMessage> {
        INSTANCE;

        @Override
        public IMessage onMessage(MessageParticleVanilla message, MessageContext ctx) {
            if (ctx.side == Side.CLIENT) {
                handleClient(message, ctx);
            }
            return null;
        }

        @SideOnly(Side.CLIENT)
        private void handleClient(MessageParticleVanilla message, MessageContext ctx) {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null) {
                if (DEBUG) {
                    BCLog.logger.warn("[lib.messages][particle.vanilla] The world was null for a message!");
                }
                return;
            } else {
                message.spawn(world);
            }
        }
    }
}
