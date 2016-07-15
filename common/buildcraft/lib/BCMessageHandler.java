/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import buildcraft.robotics.MessageZonePlannerMapChunkRequest;
import buildcraft.robotics.MessageZonePlannerMapChunkResponse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.library.network.MessageLibraryDBIndex;
import buildcraft.lib.library.network.MessageLibraryRequest;
import buildcraft.lib.library.network.MessageLibraryTransferEntry;
import buildcraft.lib.net.*;
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
        addMessageType(MessageUpdateTile.class, MessageUpdateTile.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageWidget.class, MessageWidget.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageCommand.class, MessageCommand.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageMarker.class, MessageMarker.Handler.INSTANCE, Side.CLIENT);
        addMessageType(MessageLibraryTransferEntry.class, MessageLibraryTransferEntry.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageLibraryRequest.class, MessageLibraryRequest.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageLibraryDBIndex.class, MessageLibraryDBIndex.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageParticleVanilla.class, MessageParticleVanilla.Handler.INSTANCE, Side.CLIENT);
    }

    public static void fmlPostInit() {
        netWrapper = NetworkRegistry.INSTANCE.newSimpleChannel("bc-lib");
        netWrapper.registerMessage(MessageZonePlannerMapChunkRequest.Handler.class, MessageZonePlannerMapChunkRequest.class, 123, Side.SERVER); // TODO: move it form here
        netWrapper.registerMessage(MessageZonePlannerMapChunkResponse.Handler.class, MessageZonePlannerMapChunkResponse.class, 124, Side.CLIENT);
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
            EntityPlayer player = LibProxy.getProxy().getPlayerForContext(context);
            if (player == null || player.worldObj == null) return null;
            LibProxy.getProxy().addScheduledTask(player.worldObj, () -> {
                IMessage reply = from.onMessage(message, context);
                if (reply != null) {
                    BCMessageHandler.sendReturnMessage(context, reply);
                }
            });
            return null;
        };
    }

    public static void sendReturnMessage(MessageContext context, IMessage reply) {
        // TODO This needs testing! IT MIGHT CRASH!
        // (Never used)
        EntityPlayer player = LibProxy.getProxy().getPlayerForContext(context);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            netWrapper.sendTo(reply, playerMP);
        } else if (player != null) {
            netWrapper.sendToServer(reply);
        }
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
