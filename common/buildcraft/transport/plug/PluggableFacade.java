/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.plug;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.Explosion;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.RotationUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.transport.BCTransportItems;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import buildcraft.transport.client.model.key.KeyPlugFacade;

public class PluggableFacade extends PipePluggable implements IFacade {
    public static final int SIZE = 2;
    public final FacadeInstance states;
    public final boolean isSideSolid;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, EnumFacing side, FacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
        isSideSolid = states.areAllStatesSolid(side);
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, NBTTagCompound nbt) {
        super(def, holder, side);
        this.states = FacadeInstance.readFromNbt(nbt, "states");
        activeState = MathUtil.clamp(nbt.getInteger("activeState"), 0, states.phasedStates.length - 1);
        isSideSolid = states.areAllStatesSolid(side);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        states.writeToNbt(nbt, "states");
        nbt.setInteger("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, EnumFacing side, PacketBuffer buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states = FacadeInstance.readFromBuffer(buf);
        isSideSolid = buf.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states.writeToBuffer(buf);
        buf.writeBoolean(isSideSolid);
    }

    // Pluggable methods

    @Override
    public AxisAlignedBB getBoundingBox() {
        return RotationUtil.rotateAABB(new AxisAlignedBB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, SIZE / 16D, 16 / 16D), side);
    }

    @Override
    public boolean isBlocking() {
        return !states.phasedStates[activeState].isHollow;
    }

    @Override
    public boolean isSideSolid() {
        return isSideSolid;
    }

    @Override
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return states.phasedStates[activeState].stateInfo.state.getBlock().getExplosionResistance(exploder);
    }

    @Override
    public ItemStack getPickStack() {
        return BCTransportItems.plugFacade.createItemStack(states);
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (states.type == FacadeType.Basic) {
            FacadePhasedState facadeState = states.phasedStates[activeState];
            IBlockState blockState = facadeState.stateInfo.state;
            BlockRenderLayer targetLayer = blockState.getBlock().getBlockLayer();
            if (targetLayer == BlockRenderLayer.TRANSLUCENT) {
                if (layer != targetLayer) {
                    return null;
                }
            } else if (layer == BlockRenderLayer.TRANSLUCENT) {
                return null;
            }
            return new KeyPlugFacade(layer, side, blockState, facadeState.isHollow);
        } else if (layer == BlockRenderLayer.CUTOUT) {
            return new KeyPlugBlocker(side);
        }
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(int tintIndex) {
        FacadePhasedState state = states.phasedStates[activeState];
        return Minecraft.getMinecraft().getBlockColors().colorMultiplier(state.stateInfo.state, holder.getPipeWorld(), holder.getPipePos(), tintIndex);
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return states.getType();
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return states.getPhasedStates();
    }
}
