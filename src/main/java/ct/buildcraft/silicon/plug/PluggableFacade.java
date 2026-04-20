/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.silicon.plug;

import javax.annotation.Nullable;

import ct.buildcraft.api.BCModules;
import ct.buildcraft.api.facades.FacadeType;
import ct.buildcraft.api.facades.IFacade;
import ct.buildcraft.api.facades.IFacadePhasedState;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.world.SingleBlockAccess;
import ct.buildcraft.silicon.BCSiliconItems;
import ct.buildcraft.silicon.client.model.key.KeyPlugFacade;
import ct.buildcraft.transport.client.model.key.KeyPlugBlocker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.data.ModelData;

public class PluggableFacade extends PipePluggable implements IFacade {

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 0 / 16.0;
        double max = 16 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = Shapes.box(ul, min, min, uu, max, max);
    }

    public static final int SIZE = 2;
    public final FacadeInstance states;
    public final boolean isSideSolid;
//    public final BlockFaceShape blockFaceShape;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, Direction side, FacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
        isSideSolid = states.areAllStatesSolid(side);
        //blockFaceShape = states.getBlockFaceShape(side);
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(def, holder, side);
        if (nbt.contains("states") && !nbt.contains("facade")) {
            ListTag tagStates = nbt.getList("states", Tag.TAG_COMPOUND);
            if (tagStates.size() > 0) {
                boolean isHollow = tagStates.getCompound(0).getBoolean("isHollow");
                CompoundTag tagFacade = new CompoundTag();
                tagFacade.put("states", tagStates);
                tagFacade.putBoolean("isHollow", isHollow);
                nbt.put("facade", tagFacade);
            }
        }
        this.states = FacadeInstance.readFromNbt(nbt.getCompound("facade"));
        activeState = MathUtil.clamp(nbt.getInt("activeState"), 0, states.phasedStates.length - 1);
        isSideSolid = states.areAllStatesSolid(side);
       // blockFaceShape = states.getBlockFaceShape(side);
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("facade", states.writeToNbt());
        nbt.putInt("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(def, holder, side);
        states = FacadeInstance.readFromBuffer(buffer);
        isSideSolid = buffer.readBoolean();
      // blockFaceShape = buf.readEnumValue(BlockFaceShape.class);
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        states.writeToBuffer(buffer);
        buffer.writeBoolean(isSideSolid);
       // buf.writeEnumValue(blockFaceShape);
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
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
    	BlockState state = states.phasedStates[activeState].stateInfo.state;
        return state.getBlock().getExplosionResistance(state, new SingleBlockAccess(state), BlockPos.ZERO, explosion);
    }

/*    @Override
    public BlockFaceShape getBlockFaceShape() {
        return blockFaceShape;
    }*/

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.PLUG_FACADE_ITEM.get().createItemStack(states);
    }

    @Override
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (states.type == FacadeType.Basic) {
            FacadePhasedState facadeState = states.phasedStates[activeState];
            BlockState blockState = facadeState.stateInfo.state;
            ChunkRenderTypeSet targetLayer = Minecraft.getInstance().getBlockRenderer().getBlockModel(blockState)
            		.getRenderTypes(blockState, RandomSource.create(42), ModelData.EMPTY);
            if (targetLayer.contains(RenderType.translucent())) {
                if (targetLayer.contains(layer)) {
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
