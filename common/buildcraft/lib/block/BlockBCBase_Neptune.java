/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.registry.TagManager;
import buildcraft.lib.registry.TagManager.EnumTagType;

public class BlockBCBase_Neptune extends Block {
    public static final Property<Direction> PROP_FACING = BuildCraftProperties.BLOCK_FACING;
    public static final Property<Direction> BLOCK_FACING_6 = BuildCraftProperties.BLOCK_FACING_6;

    /** The tag used to identify this in the {@link TagManager}. May be empty if not using the tag system. */
    public final String id;

    public BlockBCBase_Neptune(AbstractBlock.Settings settings, String id) {
        super(settings);
        if (id == null) {
            id = "";
        }
        this.id = id;

        // TODO(R.Chen): setUnlocalizedName / setRegistryName / setCreativeTab removed in 1.20.1;
        // registration is handled via Fabric registry events in BCCoreInitializer.

        if (this instanceof IBlockWithFacing) {
            Property<Direction> facingProp = ((IBlockWithFacing) this).getFacingProperty();
            setDefaultState(getDefaultState().with(facingProp, Direction.NORTH));
        }
    }

    // BlockState

    protected void addProperties(List<Property<?>> properties) {
        if (this instanceof IBlockWithFacing) {
            properties.add(((IBlockWithFacing) this).getFacingProperty());
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        List<Property<?>> properties = new ArrayList<>();
        addProperties(properties);
        for (Property<?> p : properties) {
            builder.add(p);
        }
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rot) {
        if (this instanceof IBlockWithFacing) {
            Property<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            state = state.with(prop, rot.rotate(state.get(prop)));
        }
        return state;
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        if (this instanceof IBlockWithFacing) {
            Property<Direction> prop = ((IBlockWithFacing) this).getFacingProperty();
            state = state.with(prop, mirror.apply(state.get(prop)));
        }
        return state;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState state = getDefaultState();
        if (this instanceof IBlockWithFacing) {
            IBlockWithFacing b = (IBlockWithFacing) this;
            net.minecraft.entity.player.PlayerEntity placer = ctx.getPlayer();
            if (placer == null) {
                return state;
            }
            Direction orientation = placer.getHorizontalFacing();
            if (b.canFaceVertically()) {
                BlockPos pos = ctx.getBlockPos();
                if (MathHelper.abs((float) (placer.getX() - pos.getX())) < 2.0F
                    && MathHelper.abs((float) (placer.getZ() - pos.getZ())) < 2.0F) {
                    double eyeY = placer.getEyeY();
                    if (eyeY - pos.getY() > 2.0D) {
                        orientation = Direction.DOWN;
                    }
                    if (pos.getY() - eyeY > 0.0D) {
                        orientation = Direction.UP;
                    }
                }
            }
            state = state.with(b.getFacingProperty(), orientation.getOpposite());
        }
        return state;
    }
}
