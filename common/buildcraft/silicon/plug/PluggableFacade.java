/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import buildcraft.api.BCModules;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.transport.client.model.key.KeyPlugBlocker;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class PluggableFacade extends PipePluggable implements IFacade {

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 0 / 16.0;
        double max = 16 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = VoxelShapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = VoxelShapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = VoxelShapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = VoxelShapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = VoxelShapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = VoxelShapes.box(ul, min, min, uu, max, max);
    }

    public static final int SIZE = 2;
    public final FacadeInstance states;
    public final boolean isSideSolid;
    // Calen: seems not still useful in 1.18.2
//    public final BlockFaceShape blockFaceShape;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, Direction side, FacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
        isSideSolid = states.areAllStatesSolid(side);
        // Calen: seems not still useful in 1.18.2
//        blockFaceShape = states.getBlockFaceShape(side);
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundNBT nbt) {
        super(def, holder, side);
        if (nbt.contains("states") && !nbt.contains("facade")) {
            ListNBT tagStates = nbt.getList("states", Constants.NBT.TAG_COMPOUND);
            if (tagStates.size() > 0) {
                boolean isHollow = tagStates.getCompound(0).getBoolean("isHollow");
                CompoundNBT tagFacade = new CompoundNBT();
                tagFacade.put("states", tagStates);
                tagFacade.putBoolean("isHollow", isHollow);
                nbt.put("facade", tagFacade);
            }
        }
        this.states = FacadeInstance.readFromNbt(nbt.getCompound("facade"));
        activeState = MathUtil.clamp(nbt.getInt("activeState"), 0, states.phasedStates.length - 1);
        isSideSolid = states.areAllStatesSolid(side);
        // Calen: seems not still useful in 1.18.2
//        blockFaceShape = states.getBlockFaceShape(side);
    }

    @Override
    public CompoundNBT writeToNbt() {
        CompoundNBT nbt = super.writeToNbt();
        nbt.put("facade", states.writeToNbt());
        nbt.putInt("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, PacketBuffer buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states = FacadeInstance.readFromBuffer(buf);
        isSideSolid = buf.readBoolean();
        // Calen: seems not still useful in 1.18.2
//        blockFaceShape = buf.readEnum(SupportType.class);
    }

    @Override
    public void writeCreationPayload(PacketBuffer buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states.writeToBuffer(buf);
        buf.writeBoolean(isSideSolid);
        // Calen: seems not still useful in 1.18.2
//        buf.writeEnum(blockFaceShape);
    }

    // Pluggable methods

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return !isHollow();
    }

    @Override
    public boolean canBeConnected() {
        return !isHollow();
    }

    @Override
    public boolean isSideSolid() {
        return isSideSolid;
    }

    @Override
//    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion)
    public float getExplosionResistance(@Nonnull Entity exploder, Explosion explosion) {
//        return states.phasedStates[activeState].stateInfo.state.getBlock().getExplosionResistance(exploder);
        BlockState state = states.phasedStates[activeState].stateInfo.state;
        World level = exploder.level;
        BlockPos pos = exploder.blockPosition();
        return state.getBlock().getExplosionResistance(state, level, pos, explosion);
    }

    // Calen: seems not still useful in 1.18.2
//    @Override
//    public BlockFaceShape getBlockFaceShape() {
//        return blockFaceShape;
//    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.plugFacade.get().createItemStack(states);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (states.type == FacadeType.Basic) {
            FacadePhasedState facadeState = states.phasedStates[activeState];
            BlockState blockState = facadeState.stateInfo.state;
//            RenderType targetLayer = blockState.getBlock().getBlockLayer();
            // Calen true -> Block  false -> Item
            RenderType targetLayer = RenderTypeLookup.getRenderType(blockState, true);
            if (targetLayer == RenderType.translucent()) {
                if (layer != targetLayer) {
                    return null;
                }
            } else if (layer == RenderType.translucent()) {
                return null;
            }
            return new KeyPlugFacade(layer, side, blockState, isHollow());
        } else if (layer == RenderType.cutout() && BCModules.TRANSPORT.isLoaded()) {
            return KeyPlugBlocker.create(side);
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(int tintIndex) {
        FacadePhasedState state = states.phasedStates[activeState];
        BlockColors colours = Minecraft.getInstance().getBlockColors();
        return colours.getColor(state.stateInfo.state, holder.getPipeWorld(), holder.getPipePos(), tintIndex);
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return states.getType();
    }

    @Override
    public boolean isHollow() {
        return states.isHollow();
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return states.getPhasedStates();
    }
}
