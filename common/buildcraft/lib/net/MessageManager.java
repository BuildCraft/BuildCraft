/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;
import com.google.common.collect.ImmutableList;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MessageManager {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");
    private static SimpleNetworkWrapper netWrapper;

    private static final Set<MessageType<?, ?>> MESSAGE_TYPES = new TreeSet<>();
    private static final Map<Integer, MessageType<?, ?>> DISCRIMINATORS_MESSAGE_TYPES = new HashMap<>();
    private static final Map<MessageType<?, ?>, Integer> MESSAGE_TYPES_DISCRIMINATORS = new HashMap<>();
    private static final Map<Pair<Side, Class<? extends IMessage>>, MessageType<?, ?>> MESSAGE_TYPES_CLASSES = new HashMap<>();

    private static final Map<Side, AtomicInteger> LAST_MESSAGE_IDS = new HashMap<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<Side, Map<Integer, ByteBuf[]>> PARTS = new HashMap<>();

    public static <I extends IMessage, O extends IMessage> void addMessageType(Class<I> messageClass,
                                                                               IMessageHandler<I, O> handler,
                                                                               boolean large,
                                                                               Side... sides) {
        if (netWrapper != null) {
            throw new IllegalStateException("Must register all messages BEFORE post-init!");
        }
        MESSAGE_TYPES.add(new MessageType<>(messageClass, handler, large, sides));
    }

    public static <I extends IMessage, O extends IMessage> void addMessageType(Class<I> messageClass,
                                                                               IMessageHandler<I, O> handler,
                                                                               Side... sides) {
        addMessageType(messageClass, handler, false, sides);
    }

    public static void fmlPostInit() {
        netWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("bc-lib");
        int i = 0;
        netWrapper.registerMessage(MessageLargeMessagePart.HANDLER, MessageLargeMessagePart.class, i++, Side.CLIENT);
        netWrapper.registerMessage(MessageLargeMessagePart.HANDLER, MessageLargeMessagePart.class, i++, Side.SERVER);
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
        if (!messageType.large) {
            netWrapper.registerMessage(handler, messageType.messageClass, discriminator, side);
        }
        MessageType<I, O> newMessageType = new MessageType<>(
            messageType.messageClass,
            handler,
            messageType.large,
            side
        );
        DISCRIMINATORS_MESSAGE_TYPES.put(discriminator, newMessageType);
        MESSAGE_TYPES_DISCRIMINATORS.put(newMessageType, discriminator);
        MESSAGE_TYPES_CLASSES.put(Pair.of(side, messageType.messageClass), newMessageType);
    }

    private static MessageType<?, ?> getMessageType(Side side, IMessage message) {
        return MESSAGE_TYPES_CLASSES.get(Pair.<Side, Class<? extends IMessage>>of(side, message.getClass()));
    }

    private static List<IMessage> getMessages(Side side, IMessage message) {
        MessageType<?, ?> messageType = getMessageType(side, message);
        if (!messageType.large) {
            return Collections.singletonList(message);
        } else {
            ByteBuf buffer = Unpooled.directBuffer();
            message.toBytes(buffer);
            ImmutableList.Builder<IMessage> messagesBuilder = new ImmutableList.Builder<>();
            int size = 1024 * 64;
            int count = buffer.capacity() / size + ((buffer.capacity() % size == 0) ? 0 : 1);
            for (int start = 0, index = 0; index < count; start += size, index++) {
                messagesBuilder.add(
                    new MessageLargeMessagePart(
                        LAST_MESSAGE_IDS.computeIfAbsent(side, s -> new AtomicInteger(0)).getAndAdd(1),
                        index,
                        count,
                        MESSAGE_TYPES_DISCRIMINATORS.get(messageType),
                        buffer.slice(start, Math.min(size, buffer.capacity() - start))
                    )
                );
            }
            return messagesBuilder.build();
        }
    }

    /**
     * Send this message to everyone.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    @SuppressWarnings("unused")
    public static void sendToAll(IMessage message) {
        getMessages(Side.CLIENT, message).forEach(netWrapper::sendToAll);
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
        getMessages(Side.CLIENT, message).forEach(m -> netWrapper.sendTo(m, player));
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
        getMessages(Side.CLIENT, message).forEach(m -> netWrapper.sendToAllAround(m, point));
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
        getMessages(Side.CLIENT, message).forEach(m -> netWrapper.sendToDimension(m, dimensionId));
    }

    /**
     * Send this message to the server.
     * The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send
     */
    public static void sendToServer(IMessage message) {
        getMessages(Side.SERVER, message).forEach(netWrapper::sendToServer);
    }

    private static class MessageType<I extends IMessage, O extends IMessage> implements Comparable<MessageType<?, ?>> {
        @Nonnull
        public final Class<I> messageClass;
        @Nonnull
        public final IMessageHandler<I, O> handler;
        // Not working
        public final boolean large;
        @Nonnull
        public final Side[] sides;

        public MessageType(@Nonnull Class<I> messageClass,
                           @Nonnull IMessageHandler<I, O> handler,
                           boolean large,
                           @Nonnull Side... sides) {
            this.messageClass = messageClass;
            this.handler = handler;
            this.large = large;
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
            if (large != that.large) {
                return false;
            }
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(sides, that.sides);
        }

        @Override
        public int hashCode() {
            int result = messageClass.hashCode();
            result = 31 * result + handler.hashCode();
            result = 31 * result + (large ? 1 : 0);
            result = 31 * result + Arrays.hashCode(sides);
            return result;
        }
    }

    public static class MessageLargeMessagePart implements IMessage {
        private int messageId;
        private int partIndex;
        private int partsCount;
        private int discriminator;
        private ByteBuf buffer;

        public MessageLargeMessagePart() {
        }

        public MessageLargeMessagePart(int messageId, int partIndex, int partsCount, int discriminator, ByteBuf buffer) {
            this.messageId = messageId;
            this.partIndex = partIndex;
            this.partsCount = partsCount;
            this.discriminator = discriminator;
            this.buffer = buffer;
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(messageId);
            buf.writeInt(partIndex);
            buf.writeInt(partsCount);
            buf.writeInt(discriminator);
            buf.writeInt(buffer.capacity());
            buf.writeBytes(buffer);
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            messageId = buf.readInt();
            partIndex = buf.readInt();
            partsCount = buf.readInt();
            discriminator = buf.readInt();
            buffer = buf.readBytes(buf.readInt());
        }

        @SuppressWarnings("unchecked")
        private static final IMessageHandler<MessageLargeMessagePart, IMessage> HANDLER = (message, ctx) -> {
            try {
                MessageType<?, ?> messageType = DISCRIMINATORS_MESSAGE_TYPES.get(message.discriminator);
                ByteBuf[] parts = PARTS.computeIfAbsent(ctx.side, s -> new HashMap<>())
                    .computeIfAbsent(message.messageId, messageId -> new ByteBuf[message.partsCount]);
                parts[message.partIndex] = message.buffer;
                System.out.println(Arrays.stream(parts).filter(Objects::nonNull).count() * 100 / parts.length + "%");
                if (Arrays.stream(parts).noneMatch(Objects::isNull)) {
                    ByteBuf buffer = Unpooled.directBuffer();
                    Arrays.stream(parts).forEach(buffer::writeBytes);
                    IMessage wholeMessage = messageType.messageClass.newInstance();
                    wholeMessage.fromBytes(buffer);
                    PARTS.get(ctx.side).remove(message.messageId);
                    return ((IMessageHandler<IMessage, IMessage>) messageType.handler).onMessage(wholeMessage, ctx);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }
}
