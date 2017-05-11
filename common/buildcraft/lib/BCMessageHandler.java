/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.MessageContainer;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.cache.MessageObjectCacheReply;
import buildcraft.lib.net.cache.MessageObjectCacheReq;
import buildcraft.lib.particle.MessageParticleVanilla;

public enum BCMessageHandler {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");
    public static SimpleNetworkWrapper netWrapper;

    private static final List<MessageTypeData<?, ?>> handlers = new ArrayList<>();

    public static <I extends IMessage, O extends IMessage> void addMessageType(Class<I> messageClass, IMessageHandler<I, O> handler, Side... sides) {
        if (netWrapper != null) throw new IllegalStateException("Must register all messages BEFORE post-init!");
        handlers.add(new MessageTypeData<>(messageClass, handler, sides));
    }

    public static void fmlPreInit() {
        addMessageType(MessageUpdateTile.class, MessageUpdateTile.HANDLER, Side.CLIENT, Side.SERVER);
        addMessageType(MessageContainer.class, MessageContainer.HANDLER, Side.CLIENT, Side.SERVER);
        addMessageType(MessageMarker.class, MessageMarker.HANDLER, Side.CLIENT);
        addMessageType(MessageParticleVanilla.class, MessageParticleVanilla.HANDLER, Side.CLIENT);
        addMessageType(MessageObjectCacheReq.class, MessageObjectCacheReq.HANDLER, Side.SERVER);
        addMessageType(MessageObjectCacheReply.class, MessageObjectCacheReply.HANDLER, Side.CLIENT);
    }

    public static void fmlPostInit() {
        netWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("bc-lib");
        Collections.sort(handlers);
        if (DEBUG) {
            BCLog.logger.info("[lib.messages] Sorted list of messages:");
            for (int i = 0; i < handlers.size(); i++) {
                final MessageTypeData<?, ?> handler = handlers.get(i);
                BCLog.logger.info("  " + i + " = " + handler.messageClass.getName() + " " + Arrays.toString(handler.sides));
            }
            BCLog.logger.info("[lib.messages] Total of " + handlers.size() + " messages");
        }
        for (int i = 0; i < handlers.size(); i++) {
            addInternal(handlers.get(i), i);
        }
    }

    private static <I extends IMessage, O extends IMessage> void addInternal(MessageTypeData<I, O> handler, int discriminator) {
        for (Side side : handler.sides) {
            netWrapper.registerMessage(wrapHandler(handler.handler), handler.messageClass, discriminator, side);
        }
    }

    /** Wraps this handler to delay message processing until the next world tick. */
    private static <I extends IMessage> IMessageHandler<I, IMessage> wrapHandler(IMessageHandler<I, ?> from) {
        return (message, context) -> {
            EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
            if (player == null || player.world == null) return null;
            BCLibProxy.getProxy().addScheduledTask(player.world, () -> {
                IMessage reply = from.onMessage(message, context);
                if (reply != null) {
                    MessageUtil.sendReturnMessage(context, reply);
                }
            });
            return null;
        };
    }

    public static class MessageTypeData<I extends IMessage, O extends IMessage> implements Comparable<MessageTypeData<?, ?>> {
        private final Class<I> messageClass;
        private final IMessageHandler<I, O> handler;
        private final Side[] sides;

        public MessageTypeData(Class<I> messageClass, IMessageHandler<I, O> handler, Side... sides) {
            this.messageClass = messageClass;
            this.handler = handler;
            this.sides = sides;
        }

        @Override
        public int compareTo(MessageTypeData<?, ?> o) {
            return messageClass.getName().compareTo(o.messageClass.getName());
        }
    }

    public static class MessageType<E extends Enum<E>> implements Comparable<MessageType<?>> {
        private final Class<E> typeClass;
        private final E[] values;

        public MessageType(Class<E> typeClass) {
            this.typeClass = typeClass;
            this.values = typeClass.getEnumConstants();
            if (values == null || values.length == 0) throw new IllegalArgumentException("No enum constants for class " + typeClass);
        }

        @Override
        public int compareTo(MessageType<?> o) {
            return typeClass.getName().compareTo(o.typeClass.getName());
        }
    }
}
