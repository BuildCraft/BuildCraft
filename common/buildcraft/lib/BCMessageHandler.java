/* Copyright (c) 2016 AlexIIL and the BuildCraft team
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package buildcraft.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import buildcraft.lib.net.MessageCommand;
import buildcraft.lib.net.MessageMarker;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.MessageWidget;

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
