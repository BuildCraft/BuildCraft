/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.player.EntityPlayer;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.gui.ContainerBC_Neptune;
import buildcraft.lib.misc.data.IdAllocator;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.builders.filling.IParameter;
import buildcraft.core.marker.volume.ClientVolumeBoxes;
import buildcraft.core.marker.volume.EnumAddonSlot;
import buildcraft.core.marker.volume.VolumeBox;
import buildcraft.core.marker.volume.WorldSavedDataVolumeBoxes;

public class ContainerFillingPlanner extends ContainerBC_Neptune implements IContainerFilling {
    private static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("filling_planner");
    private static final int ID_DATA = IDS.allocId("DATA");

    public AddonFillingPlanner addon;
    private List<IParameter> parameters = new ArrayList<>();
    private boolean inverted;

    public ContainerFillingPlanner(EntityPlayer player) {
        super(player);
        Pair<VolumeBox, EnumAddonSlot> selectingBoxAndSlot = player.world.isRemote ?
                EnumAddonSlot.getSelectingBoxAndSlot(player, ClientVolumeBoxes.INSTANCE) :
                EnumAddonSlot.getSelectingBoxAndSlot(player, WorldSavedDataVolumeBoxes.get(player.world));
        addon = (AddonFillingPlanner) selectingBoxAndSlot.getLeft().addons.get(selectingBoxAndSlot.getRight());
        parameters.addAll(addon.parameters);
        inverted = addon.inverted;
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void sendDataToServer() {
        sendMessage(ID_DATA, buffer -> {
            buffer.writeInt(parameters.size());
            parameters.forEach(parameter -> IParameter.toBytes(buffer, parameter));
            buffer.writeBoolean(inverted);
        });
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == Side.SERVER) {
            if (id == ID_DATA) {
                parameters.clear();
                IntStream.range(0, buffer.readInt())
                    .mapToObj(i -> IParameter.fromBytes(buffer))
                    .forEach(parameters::add);
                inverted = buffer.readBoolean();
                addon.parameters.clear();
                addon.parameters.addAll(parameters);
                addon.inverted = inverted;
                addon.updateBuildingInfo();
                WorldSavedDataVolumeBoxes.get(player.world).markDirty();
            }
        }
    }

    @Override
    public boolean isInverted() {
        return inverted;
    }

    @Override
    public void setInverted(boolean value) {
        inverted = value;
        sendDataToServer();
    }

    @Override
    public List<IParameter> getParameters() {
        return ImmutableList.copyOf(parameters);
    }

    @Override
    public void setParameters(List<IParameter> value) {
        parameters.clear();
        parameters.addAll(value);
    }

    @Override
    public boolean isCanExcavate() {
        return true;
    }

    @Override
    public void setCanExcavate(boolean value) {
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
