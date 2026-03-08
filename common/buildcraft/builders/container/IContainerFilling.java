/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import buildcraft.api.filler.IFillerPattern;
import buildcraft.builders.filler.FillerType;
import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.statement.FullStatement;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;
import java.util.stream.IntStream;

public interface IContainerFilling {
    Player getPlayer();

    void sendMessage(int id, IPayloadWriter writer);

    FullStatement<IFillerPattern> getPatternStatementClient();

    FullStatement<IFillerPattern> getPatternStatement();

    boolean isInverted();

    void setInverted(boolean value);

    default boolean isLocked() {
        return false;
    }

    void valuesChanged();

    default void init() {
        if (!getPlayer().level.isClientSide) {
            MessageUtil.doDelayedServer(this::sendData);
        }
    }

    default void sendData() {
        sendMessage(ContainerBC_Neptune.NET_DATA, buffer ->
        {
            (getPlayer().level.isClientSide
                    ? getPatternStatementClient()
                    : getPatternStatement()).writeToBuffer(buffer);
            buffer.writeBoolean(isInverted());
        });
    }

    default void onStatementChange() {
        sendData();
    }

    default void sendInverted(boolean value) {
        setInverted(value);
        sendData();
    }

    // default void readMessage(int id, PacketBufferBC buffer, Dist side, MessageContext ctx) throws IOException
    default void readMessage(int id, PacketBufferBC buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        if (side == NetworkDirection.PLAY_TO_SERVER) {
            if (id == ContainerBC_Neptune.NET_DATA) {
                if (isLocked()) {
                    new FullStatement<>(
                            FillerType.INSTANCE,
                            4,
                            (a, b) ->
                            {
                            }
                    ).readFromBuffer(buffer);
                } else {
                    getPatternStatement().readFromBuffer(buffer);
                }
                setInverted(buffer.readBoolean());
                valuesChanged();
                sendData();
            }
        } else if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == ContainerBC_Neptune.NET_DATA) {
                getPatternStatement().readFromBuffer(buffer);
                setInverted(buffer.readBoolean());
                getPatternStatementClient().set(getPatternStatement().get());
                IntStream.range(0, 4).forEach(i ->
                        getPatternStatementClient().getParamRef(i).set(getPatternStatement().getParamRef(i).get())
                );
                valuesChanged();
            }
        }
    }
}
