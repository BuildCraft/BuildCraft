/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.api.transport.pipe.PipeApi;
import ct.buildcraft.api.transport.pipe.PipeDefinition;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.transport.BCTransportBlocks;
import ct.buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SchematicBlockPipe implements ISchematicBlock {
    private CompoundTag tileNbt;
    private Rotation tileRotation = Rotation.NONE;

    public static boolean predicate(SchematicBlockContext context) {
        return context.world.getBlockState(context.pos).getBlock() == BCTransportBlocks.pipeHolder.get();
    }

    @Override
    public void init(SchematicBlockContext context) {
        BlockEntity tileEntity = context.world.getBlockEntity(context.pos);
        if (tileEntity == null) {
            throw new IllegalStateException();
        }
        tileNbt = tileEntity.serializeNBT();//TODO change to saveWithId()
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        try {
            ImmutableList.Builder<ItemStack> builder = ImmutableList.builder();
            PipeDefinition definition = PipeRegistry.INSTANCE.loadDefinition(
                tileNbt.getCompound("pipe").getString("def")
            );
            DyeColor color = NBTUtilBC.readEnum(
                tileNbt.getCompound("pipe").get("col"),
                DyeColor.class
            );
            Item item = (Item) PipeApi.pipeRegistry.getItemForPipe(definition);
            ItemStack stack = new ItemStack(item, 1);
            if (item != null&&color != null) {
            	CompoundTag tag = new CompoundTag();
            	tag.putInt("color", color.getId() + 1);
    			stack.setTag(tag);
            }
            builder.add(stack);
            return builder.build();
        } catch (InvalidInputDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SchematicBlockPipe getRotated(Rotation rotation) {
        SchematicBlockPipe schematicBlock = new SchematicBlockPipe();
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.tileRotation = tileRotation.getRotated(rotation);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level world, BlockPos blockPos) {
        return world.isEmptyBlock(blockPos);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean build(Level world, BlockPos blockPos) {
        if (world.setBlock(blockPos, BCTransportBlocks.pipeHolder.get().defaultBlockState(), 11)) {
            BlockEntity tileEntity = BlockEntity.loadStatic(blockPos, BCTransportBlocks.pipeHolder.get().defaultBlockState(),tileNbt);
            if (tileEntity != null) {
                tileEntity.setLevel(world);
                world.setBlockEntity(tileEntity);
                if (tileRotation != Rotation.NONE && tileEntity instanceof TilePipeHolder pipeTile) {
                	pipeTile.rotate(tileRotation);
                }
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean buildWithoutChecks(Level world, BlockPos blockPos) {
        if (world.setBlock(blockPos, BCTransportBlocks.pipeHolder.get().defaultBlockState(), 0)) {
            BlockEntity tileEntity = BlockEntity.loadStatic(blockPos, BCTransportBlocks.pipeHolder.get().defaultBlockState(),tileNbt);
            if (tileEntity != null) {
                tileEntity.setLevel(world);
                world.setBlockEntity(tileEntity);
                world.updateNeighbourForOutputSignal(blockPos, BCTransportBlocks.pipeHolder.get());
                if (tileRotation != Rotation.NONE && tileEntity instanceof TilePipeHolder pipeTile) {
                	pipeTile.rotate(tileRotation);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBuilt(Level world, BlockPos blockPos) {
    	CompoundTag copy = tileNbt.copy();
    	CompoundTag tileTag = null;
    	if(world.getBlockEntity(blockPos) instanceof TilePipeHolder tile) {
	    	copy.putInt("x", blockPos.getX());
	    	copy.putInt("y", blockPos.getY());
	    	copy.putInt("z", blockPos.getZ());
	    	tile.rotate(tileRotation);
	    	tileTag = tile.serializeNBT();
	    	int ordinal = tileRotation.ordinal();
	    	int inverseId = ordinal ^ ((ordinal&1) << 1);
			tile.rotate(Rotation.values()[inverseId]);
    	}
		return tileTag != null && copy.equals(tileTag);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("tileNbt", tileNbt);
        nbt.put("tileRotation", NBTUtilBC.writeEnum(tileRotation));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        tileNbt = nbt.getCompound("tileNbt");
        tileRotation = NBTUtilBC.readEnum(nbt.get("tileRotation"), Rotation.class);
    }
}
