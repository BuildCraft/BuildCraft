package buildcraft.lib;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.net.MessageCommand;
import buildcraft.lib.net.MessageWidget;

public enum BCMessageHandler {
    INSTANCE;

    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");
    public static SimpleNetworkWrapper netWrapper;

    private static final List<MessageTypeData<?, ?>> handlers = new ArrayList<>();

    public static <REQ extends IMessage, REPLY extends IMessage> void addMessageType(Class<REQ> messageClass, IMessageHandler<REQ, REPLY> handler, Side... sides) {
        handlers.add(new MessageTypeData<>(messageClass, handler, sides));
    }

    public static void preInit() {
        addMessageType(MessageCommand.class, MessageCommand.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
        addMessageType(MessageWidget.class, MessageWidget.Handler.INSTANCE, Side.CLIENT, Side.SERVER);
    }

    public static void init() {
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

    private static <REQ extends IMessage, REPLY extends IMessage> void addInternal(MessageTypeData<REQ, REPLY> handler, int discriminator) {
        for (Side side : handler.sides) {
            netWrapper.registerMessage(handler.handler, handler.messageClass, discriminator, side);
        }
    }

    public static class MessageTypeData<REQ extends IMessage, REPLY extends IMessage> implements Comparable<MessageTypeData<?, ?>> {
        private final Class<REQ> messageClass;
        private final IMessageHandler<REQ, REPLY> handler;
        private final Side[] sides;

        public MessageTypeData(Class<REQ> messageClass, IMessageHandler<REQ, REPLY> handler, Side... sides) {
            this.messageClass = messageClass;
            this.handler = handler;
            this.sides = sides;
        }

        @Override
        public int compareTo(MessageTypeData<?, ?> o) {
            return messageClass.getName().compareTo(o.messageClass.getName());
        }
    }
}
