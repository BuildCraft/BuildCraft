/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;

public class MessageManager {
    public enum MessageId {
        // ID Allocation:
        // If you (an addon mod) want to add messages to be sent and received through this system
        // then PR a network ID range to allocate. (Along with your modid and where the source is
        // located so that we can keep track of it)

        // BuildCraft: Reserves ID's 00-1F
        BC_LIB_TILE_UPDATE(0x00),
        BC_LIB_CONTAINER(0x01),
        BC_LIB_MARKER(0x02),
        BC_LIB_CACHE_REQUEST(0x03),
        BC_LIB_CACHE_REPLY(0x04),
        BC_LIB_DEBUG_REQUEST(0x05),
        BC_LIB_DEBUG_REPLY(0x06),

        BC_CORE_VOLUME_BOX(0x07),

        BC_BUILDERS_SNAPSHOT_REQUEST(0x08),
        BC_BUILDERS_SNAPSHOT_REPLY(0x09),

        BC_SILICON_WIRE_NETWORK(0x0A),
        BC_SILICON_WIRE_SWITCH(0x0B),

        BC_ROBOTICS_ZONE_REQUEST(0x0C),
        BC_ROBOTICS_ZONE_REPLY(0x0D),
        BC_ROBOTICS_ROBOTS(0x0E);
        // End of BuildCraft

        static {
            // Sanity check
            Map<Integer, MessageId> ids = new HashMap<>();
            for (MessageId type : values()) {
                MessageId existing = ids.put(type.id, type);
                if (existing != null) {
                    throw new Error("Duplicate ID's -- " + type + " is the same as " + existing);
                }
            }
        }

        public final int id;

        MessageId(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return name() + " (#" + id + ")";
        }
    }

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");
    private static final SimpleNetworkWrapper netWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("bc-lib");
    private static final Map<MessageId, Class<?>> registeredMessages = new EnumMap<>(MessageId.class);
    private static final Map<Class<?>, MessageId> knownMessageClasses = new HashMap<>();

    /** Registers a message as one that will not be received, but will be sent. */
    public static <I extends IMessage> void addTypeSent(MessageId id, Class<I> clazz, Side recv) {
        addType(id, clazz, null, recv);
    }

    public static <I extends IMessage> void addType(MessageId id, Class<I> messageClass, IMessageHandler<I, ?> handler,
        Side... sides) {
        IMessageHandler<I, ?> wrapped;
        Class<?> prev = registeredMessages.get(id);
        if (prev != null) {
            throw new IllegalStateException(
                "Already registered handler for " + id + " as " + prev + " ( new = " + messageClass + ")");
        }
        registeredMessages.put(id, messageClass);
        knownMessageClasses.put(messageClass, id);
        if (handler != null) {
            wrapped = wrapHandler(handler);
        } else {
            wrapped = throwingHandler(messageClass);
        }
        if (sides == null || sides.length == 0) {
            sides = Side.values();
        }
        for (Side side : sides) {
            netWrapper.registerMessage(wrapped, messageClass, id.id, side);
        }
    }

    private static <I extends IMessage> IMessageHandler<I, ?> throwingHandler(Class<I> clazz) {
        return (message, context) -> {
            if (context.side == Side.SERVER) {
                // Bad/Buggy client
                EntityPlayerMP player = context.getServerHandler().player;
                BCLog.logger
                    .warn("[lib.messages] The client " + player.getName() + " (ID = " + player.getGameProfile().getId()
                        + ") sent an invalid message " + clazz + ", when they should only receive them!");
            } else {
                throw new Error("Received message " + clazz
                    + " on the client, when it should only be sent by the client and received on the server!");
            }
            return null;
        };
    }

    public static <I extends IMessage> IMessageHandler<I, ?> wrapHandler(IMessageHandler<I, ?> handler) {
        return (message, context) -> {
            EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
            if (player == null || player.world == null) {
                return null;
            }
            BCLibProxy.getProxy().addScheduledTask(player.world, () -> {
                IMessage reply = handler.onMessage(message, context);
                if (reply != null) {
                    MessageUtil.sendReturnMessage(context, reply);
                }
            });
            return null;
        };
    }

    /** Send this message to everyone. The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send */
    public static void sendToAll(IMessage message) {
        validateSendingMessage(message);
        netWrapper.sendToAll(message);
    }

    /** Send this message to the specified player. The {@link IMessageHandler} for this message type should be on the
     * CLIENT side.
     *
     * @param message The message to send
     * @param player The player to send it to */
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        validateSendingMessage(message);
        netWrapper.sendTo(message, player);
    }

    /** Send this message to everyone within a certain range of a point. The {@link IMessageHandler} for this message
     * type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param point The {@link net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint} around which to
     *            send */
    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        validateSendingMessage(message);
        netWrapper.sendToAllAround(message, point);
    }

    /** Send this message to everyone within the supplied dimension. The {@link IMessageHandler} for this message type
     * should be on the CLIENT side.
     *
     * @param message The message to send
     * @param dimensionId The dimension id to target */
    public static void sendToDimension(IMessage message, int dimensionId) {
        validateSendingMessage(message);
        netWrapper.sendToDimension(message, dimensionId);
    }

    /** Send this message to the server. The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send */
    public static void sendToServer(IMessage message) {
        validateSendingMessage(message);
        netWrapper.sendToServer(message);
    }

    private static void validateSendingMessage(IMessage message) {
        if (DEBUG) {
            Class<?> msgClass = message.getClass();
            for (Class<?> cls : knownMessageClasses.keySet()) {
                if (cls == msgClass) {
                    return;
                }
            }
            throw new IllegalArgumentException("Unknown/unregistered message " + msgClass);
        }
    }
}
