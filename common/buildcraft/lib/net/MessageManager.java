/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.BCModules;
import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLibProxy;
import buildcraft.lib.misc.MessageUtil;

public class MessageManager {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");

    private static final Map<BCModules, SimpleNetworkWrapper> SIMPLE_NETWORK_WRAPPERS = new EnumMap<>(BCModules.class);
    private static final Map<BCModules, Integer> LAST_IDS = new EnumMap<>(BCModules.class);
    private static final Map<Class<? extends IMessage>, BCModules> MESSAGE_CLASSES_MODULES = new HashMap<>();
    private static final Map<Class<? extends IMessage>, Integer> MESSAGE_CLASSES_IDS = new HashMap<>();
    private static final Set<Class<? extends IMessage>> MESSAGE_CLASSES_WITH_HANDLER_REGISTERED = new HashSet<>();

    static {
        for (BCModules module : BCModules.VALUES) {
            SIMPLE_NETWORK_WRAPPERS.put(module, NetworkRegistry.INSTANCE.newSimpleChannel(module.getModId()));
            LAST_IDS.put(module, 0);
        }
    }

    /**
     * Registers a message as one that will not be received, but will be sent.
     */
    public static <I extends IMessage> void registerMessageClass(BCModules module,
                                                                 Class<I> clazz,
                                                                 Side... sides) {
        registerMessageClass(module, clazz, null, sides);
    }

    public static <I extends IMessage> void registerMessageClass(BCModules module,
                                                                 Class<I> messageClass,
                                                                 IMessageHandler<I, ?> messageHandler,
                                                                 Side... sides) {
        if (MESSAGE_CLASSES_MODULES.containsKey(messageClass)) {
            throw new IllegalArgumentException("Already registered message: " + messageClass);
        }
        MESSAGE_CLASSES_MODULES.put(messageClass, module);
        MESSAGE_CLASSES_IDS.put(messageClass, LAST_IDS.get(module));
        LAST_IDS.put(module, LAST_IDS.get(module) + 1);
        if (messageHandler != null) {
            MESSAGE_CLASSES_WITH_HANDLER_REGISTERED.add(messageClass);
        }
        for (Side side : sides.length != 0 ? sides : Side.values()) {
            SIMPLE_NETWORK_WRAPPERS.get(module).registerMessage(
                wrapHandler(messageHandler, messageClass),
                messageClass,
                MESSAGE_CLASSES_IDS.get(messageClass),
                side
            );
        }
    }

    public static <I extends IMessage> void setHandler(Class<I> messageClass,
                                                       IMessageHandler<I, ?> messageHandler,
                                                       Side side) {
        if (!MESSAGE_CLASSES_MODULES.containsKey(messageClass)) {
            throw new IllegalArgumentException("Can not set handler for unregistered message: " + messageClass);
        }
        if (MESSAGE_CLASSES_WITH_HANDLER_REGISTERED.contains(messageClass)) {
            throw new IllegalArgumentException("Already registered handler for message: " + messageClass);
        }
        SIMPLE_NETWORK_WRAPPERS.get(MESSAGE_CLASSES_MODULES.get(messageClass)).registerMessage(
            wrapHandler(messageHandler, messageClass),
            messageClass,
            MESSAGE_CLASSES_IDS.get(messageClass),
            side
        );
    }

    private static <I extends IMessage> IMessageHandler<I, ?> wrapHandler(IMessageHandler<I, ?> messageHandler,
                                                                          Class<I> messageClass) {
        if (messageHandler == null) {
            return (message, context) -> {
                if (context.side == Side.SERVER) {
                    // Bad/Buggy client
                    EntityPlayerMP player = context.getServerHandler().player;
                    BCLog.logger
                        .warn("[lib.messages] The client " + player.getName() + " (ID = " + player.getGameProfile().getId()
                            + ") sent an invalid message " + messageClass + ", when they should only receive them!");
                } else {
                    throw new Error("Received message " + messageClass
                        + " on the client, when it should only be sent by the client and received on the server!");
                }
                return null;
            };
        } else {
            return (message, context) -> {
                EntityPlayer player = BCLibProxy.getProxy().getPlayerForContext(context);
                if (player == null || player.world == null) {
                    return null;
                }
                BCLibProxy.getProxy().addScheduledTask(player.world, () -> {
                    IMessage reply = messageHandler.onMessage(message, context);
                    if (reply != null) {
                        MessageUtil.sendReturnMessage(context, reply);
                    }
                });
                return null;
            };
        }
    }

    private static SimpleNetworkWrapper getSimpleNetworkWrapper(IMessage message) {
        Class<? extends IMessage> messageClass = message.getClass();
        if (!MESSAGE_CLASSES_MODULES.containsKey(messageClass)) {
            throw new IllegalArgumentException("Can not send unregistered message " + messageClass);
        }
        return SIMPLE_NETWORK_WRAPPERS.get(MESSAGE_CLASSES_MODULES.get(messageClass));
    }

    /**
     * Send this message to everyone. The {@link IMessageHandler} for this message type should be on the CLIENT side.
     *
     * @param message The message to send
     */
    @SuppressWarnings("unused")
    public static void sendToAll(IMessage message) {
        getSimpleNetworkWrapper(message).sendToAll(message);
    }

    /**
     * Send this message to the specified player. The {@link IMessageHandler} for this message type should be on the
     * CLIENT side.
     *
     * @param message The message to send
     * @param player  The player to send it to
     */
    @SuppressWarnings("unused")
    public static void sendTo(IMessage message, EntityPlayerMP player) {
        getSimpleNetworkWrapper(message).sendTo(message, player);
    }

    /**
     * Send this message to everyone within a certain range of a point. The {@link IMessageHandler} for this message
     * type should be on the CLIENT side.
     *
     * @param message The message to send
     * @param point   The {@link net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint} around which to
     *                send
     */
    @SuppressWarnings("unused")
    public static void sendToAllAround(IMessage message, NetworkRegistry.TargetPoint point) {
        getSimpleNetworkWrapper(message).sendToAllAround(message, point);
    }

    /**
     * Send this message to everyone within the supplied dimension. The {@link IMessageHandler} for this message type
     * should be on the CLIENT side.
     *
     * @param message     The message to send
     * @param dimensionId The dimension id to target
     */
    @SuppressWarnings("unused")
    public static void sendToDimension(IMessage message, int dimensionId) {
        getSimpleNetworkWrapper(message).sendToDimension(message, dimensionId);
    }

    /**
     * Send this message to the server. The {@link IMessageHandler} for this message type should be on the SERVER side.
     *
     * @param message The message to send
     */
    @SuppressWarnings("unused")
    public static void sendToServer(IMessage message) {
        getSimpleNetworkWrapper(message).sendToServer(message);
    }
}
