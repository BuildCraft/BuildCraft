/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.misc.NBTUtilBC;

import buildcraft.transport.BCTransportBlocks;

public class SchematicBlockPipe implements ISchematicBlock {
    private NBTTagCompound tileNbt;
    private Rotation tileRotation = Rotation.NONE;

    public static boolean predicate(SchematicBlockContext context) {
        return context.world.getBlockState(context.pos).getBlock() == BCTransportBlocks.pipeHolder;
    }

    @Override
    public void init(SchematicBlockContext context) {
        TileEntity tileEntity = context.world.getTileEntity(context.pos);
        if (tileEntity == null) {
            throw new IllegalStateException();
        }
        tileNbt = tileEntity.serializeNBT();
    }

    @Override
    public boolean isAir() {
        return false;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return Collections.emptySet();
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(SchematicBlockContext context) {
        try {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            PipeDefinition definition = PipeRegistry.INSTANCE.loadDefinition(
                tileNbt.getCompoundTag("pipe").getString("def")
            );
            EnumDyeColor color = NBTUtilBC.readEnum(
                tileNbt.getCompoundTag("pipe").getTag("col"),
                EnumDyeColor.class
            );
            Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(definition);
            if (item != null) {
                builder.add(
                    new ItemStack(
                        item,
                        1,
                        color == null ? 0 : color.getMetadata() + 1
                    )
                );
            }
            return builder.build();
        } catch (InvalidInputDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids(SchematicBlockContext context) {
        return Collections.emptyList();
    }

    @Override
    public SchematicBlockPipe getRotated(Rotation rotation) {
        SchematicBlockPipe schematicBlock = new SchematicBlockPipe();
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.tileRotation = tileRotation.add(rotation);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isAirBlock(blockPos);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean build(World world, BlockPos blockPos) {
        if (world.setBlockState(blockPos, BCTransportBlocks.pipeHolder.getDefaultState(), 11)) {
            TileEntity tileEntity = TileEntity.create(world, tileNbt);
            if (tileEntity != null) {
                tileEntity.setWorld(world);
                world.setTileEntity(blockPos, tileEntity);
                if (tileRotation != Rotation.NONE) {
                    tileEntity.rotate(tileRotation);
                }
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean buildWithoutChecks(World world, BlockPos blockPos) {
        if (world.setBlockState(blockPos, BCTransportBlocks.pipeHolder.getDefaultState(), 0)) {
            TileEntity tileEntity = TileEntity.create(world, tileNbt);
            if (tileEntity != null) {
                tileEntity.setWorld(world);
                world.setTileEntity(blockPos, tileEntity);
                if (tileRotation != Rotation.NONE) {
                    tileEntity.rotate(tileRotation);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return world.getBlockState(blockPos).getBlock() == BCTransportBlocks.pipeHolder;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("tileNbt", tileNbt);
        nbt.setTag("tileRotation", NBTUtilBC.writeEnum(tileRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        tileNbt = nbt.getCompoundTag("tileNbt");
        tileRotation = NBTUtilBC.readEnum(nbt.getTag("tileRotation"), Rotation.class);
    }
}
