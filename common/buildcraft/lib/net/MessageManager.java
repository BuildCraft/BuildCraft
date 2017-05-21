/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class MessageManager {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");
    private static SimpleNetworkWrapper netWrapper;

    private static final Set<MessageType<?, ?>> MESSAGE_TYPES = new TreeSet<>();


    public static <I extends IMessage, O extends IMessage> void addMessageType(Class<I> messageClass,
                                                                               IMessageHandler<I, O> handler,
                                                                               Side... sides) {
        if (netWrapper != null) {
            throw new IllegalStateException("Must register all messages BEFORE post-init!");
        }
        MESSAGE_TYPES.add(new MessageType<>(messageClass, handler, sides));
    }

    public static void fmlPostInit() {
        netWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("bc-lib");
        int i = 0;
        if (DEBUG) {
            BCLog.logger.info("[lib.messages] Sorted list of messages:");
        }
        for (MessageType<?, ?> messageType : MESSAGE_TYPES) {
            for (Side side : messageType.sides) {
                if (DEBUG) {
                    BCLog.logger.info("  " + i + " = " +
                        messageType.messageClass.getName() + " " +
                        Arrays.toString(messageType.sides));
                }
                addInternal(messageType, i++, side);
            }
        }
        if (DEBUG) {
            BCLog.logger.info("[lib.messages] Total of " + MESSAGE_TYPES.size() + " messages");
        }
    }

    private static <I extends IMessage, O extends IMessage> void addInternal(MessageType<I, O> messageType,
                                                                             int discriminator,
                                                                             Side side) {
        IMessageHandler<I, O> handler = (message, context) -> {
            EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
            if (player == null || player.world == null) {
                return null;
            }
            BCLibProxy.getProxy().addScheduledTask(player.world, () -> {
                IMessage reply = messageType.handler.onMessage(message, context);
                if (reply != null) {
                    MessageUtil.sendReturnMessage(context, reply);
                }
            });
            return null;
        };
        netWrapper.registerMessage(handler, messageType.messageClass, discriminator, side);
    }

    /**
     * Send this message to everyone.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    @SuppressWarnings("unused")
    public static void sendToAll(IMessage message) {
        netWrapper.sendToAll(message);
    }

    /**
     * Send this message to the specified player.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    @SuppressWarnings("unused")
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        netWrapper.sendTo(message, player);
    }

    /**
     * Send this message to everyone within a certain range of a point.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param point   The {@link NetworkRegistry.TargetPoint} around which to send
     */
    @SuppressWarnings("unused")
    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        netWrapper.sendToAllAround(message, point);
    }

    /**
     * Send this message to everyone within the supplied dimension.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    @SuppressWarnings("unused")
    public static void sendToDimension(IMessage message, int dimensionId) {
        netWrapper.sendToDimension(message, dimensionId);
    }

    /**
     * Send this message to the server.
     * The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send
     */
    public static void sendToServer(IMessage message) {
        netWrapper.sendToServer(message);
    }

    private static class MessageType<I extends IMessage, O extends IMessage> implements Comparable<MessageType<?, ?>> {
        @Nonnull
        public final Class<I> messageClass;
        @Nonnull
        public final IMessageHandler<I, O> handler;
        @Nonnull
        public final Side[] sides;

        public MessageType(@Nonnull Class<I> messageClass,
                           @Nonnull IMessageHandler<I, O> handler,
                           @Nonnull Side... sides) {
            this.messageClass = messageClass;
            this.handler = handler;
            this.sides = sides;
        }

        @Override
        public int compareTo(@Nonnull MessageType<?, ?> o) {
            if (messageClass != o.messageClass) {
                return messageClass.getName().compareTo(o.messageClass.getName());
            } else if (sides.length != o.sides.length) {
                return Integer.compare(sides.length, o.sides.length);
            } else {
                return sides.length != 1 ? 0 : sides[0].compareTo(o.sides[0]);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MessageType<?, ?> that = (MessageType<?, ?>) o;

            if (!messageClass.equals(that.messageClass)) {
                return false;
            }
            if (!handler.equals(that.handler)) {
                return false;
            }
            return Arrays.equals(sides, that.sides);
        }

        @Override
        public int hashCode() {
            int result = messageClass.hashCode();
            result = 31 * result + handler.hashCode();
            result = 31 * result + Arrays.hashCode(sides);
            return result;
        }
    }
}
