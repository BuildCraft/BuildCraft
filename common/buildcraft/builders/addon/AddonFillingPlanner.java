/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.addon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.lib.misc.NBTUtilBC;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.filling.Filling;
import buildcraft.builders.filling.IParameter;
import buildcraft.builders.snapshot.Template;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.AddonDefaultRenderer;
import buildcraft.core.marker.volume.IFastAddonRenderer;
import buildcraft.core.marker.volume.ISingleAddon;

public class AddonFillingPlanner extends Addon implements ISingleAddon {
    public final Set<Runnable> updateBuildingInfoListeners = new HashSet<>();
    public final List<IParameter> parameters = new ArrayList<>();
    public boolean inverted;
    public Template.BuildingInfo buildingInfo;

    public void updateBuildingInfo() {
        buildingInfo = Filling.createBuildingInfo(
            box.box.min(),
            box.box.size(),
            parameters,
            inverted
        );
        updateBuildingInfoListeners.forEach(Runnable::run);
    }

    @Override
    public void onBoxSizeChange() {
        updateBuildingInfo();
    }

    @Override
    public IFastAddonRenderer<AddonFillingPlanner> getRenderer() {
        return new AddonDefaultRenderer<AddonFillingPlanner>(BCBuildersSprites.FILLING_PLANNER.getSprite())
                .then(new AddonRendererFillingPlanner());
    }

    @Override
    public void onAdded() {
        parameters.addAll(Filling.initParameters());
        updateBuildingInfo();
    }

    @Override
    public void onPlayerRightClick(EntityPlayer player) {
        BCBuildersGuis.FILLING_PLANNER.openGUI(player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setTag(
                "parameters",
                NBTUtilBC.writeCompoundList(
                        parameters.stream()
                                .map(parameter -> IParameter.writeToNBT(new NBTTagCompound(), parameter))
                )
        );
        nbt.setBoolean("inverted", inverted);
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        NBTUtilBC.readCompoundList(nbt.getTag("parameters"))
            .map(IParameter::readFromNBT)
            .forEach(parameters::add);
        inverted = nbt.getBoolean("inverted");
        updateBuildingInfo();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(parameters.size());
        parameters.forEach(parameter -> IParameter.toBytes(buf, parameter));
        buf.writeBoolean(inverted);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        parameters.clear();
        IntStream.range(0, buf.readInt()).mapToObj(i -> IParameter.fromBytes(buf)).forEach(parameters::add);
        inverted = buf.readBoolean();
        updateBuildingInfo();
    }
}
