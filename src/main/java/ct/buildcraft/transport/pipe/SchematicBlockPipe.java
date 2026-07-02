/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.pipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.SchematicBlockContext;
import ct.buildcraft.lib.misc.NBTUtilBC;
import ct.buildcraft.transport.BCTransportBlocks;
import ct.buildcraft.transport.block.BlockPipeHolder;
import ct.buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class SchematicBlockPipe implements ISchematicBlock {
    private CompoundTag tileNbt;
    private Rotation tileRotation = Rotation.NONE;
    
    private List<ItemStack> requiredItems = null;
    private List<FluidStack> requFluidStacks = null;

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
    public List<ItemStack> computeRequiredItems(Level level) {
    	if(requiredItems == null)
    		buildRequireCache(level);
    	return requiredItems;
    }
    
    

    @Override
	public List<FluidStack> computeRequiredFluids(Level level) {
		if(requFluidStacks == null)
			buildRequireCache(level);
		return requFluidStacks;
	}
    
    private void buildRequireCache(Level level) {
    	if(level instanceof ServerLevel serverLevel) {
			BlockPipeHolder pipeBlock = BCTransportBlocks.pipeHolder.get();
			BlockState defaultBlockState = pipeBlock.defaultBlockState();
			BlockEntity tile = BlockEntity.loadStatic(BlockPos.ZERO, defaultBlockState, tileNbt);
			tile.setLevel(serverLevel);
	    	List<ItemStack> require = pipeBlock.getDrops(defaultBlockState, new Builder(serverLevel)
	    			.withOptionalParameter(LootContextParams.ORIGIN, Vec3.ZERO)
	    			.withOptionalParameter(LootContextParams.BLOCK_ENTITY, tile));
	    	tile.setRemoved();
	    	
	    	List<ItemStack> notFluidItem = require.stream().filter((item) -> !(FluidUtil.getFluidHandler(item).isPresent())).toList();
	    	List<ItemStack> fluidItem = new ArrayList<>();
	    	
	    	requFluidStacks = require.stream().map(FluidUtil::getFluidHandler)
	    		.map(opt -> opt.lazyMap(fluidHandler -> {
	    			FluidStack fluid = fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE);
					ItemStack container = fluidHandler.getContainer();
					if(!container.isEmpty())
						fluidItem.add(container);
	    			return fluid;
	    		}))
	    		.filter(LazyOptional::isPresent).map(opt -> opt.orElse(FluidStack.EMPTY)).toList();
	    	
	    	fluidItem.addAll(notFluidItem);
	    	requiredItems = fluidItem;
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
    	BlockEntity worldTile = world.getBlockEntity(blockPos);
		if(worldTile instanceof TilePipeHolder tile) {
	    	copy.putInt("x", blockPos.getX());
	    	copy.putInt("y", blockPos.getY());
	    	copy.putInt("z", blockPos.getZ());
	    	int ordinal = tileRotation.ordinal();
	    	int inverseId = ordinal ^ ((ordinal&1) << 1);
			tile.rotate(Rotation.values()[inverseId]);
	    	tileTag = tile.serializeNBT();
	    	tile.rotate(tileRotation);
    	}
//		if(worldTile == null)
			
		boolean flag2 = tileTag != null && copy.equals(tileTag);
		return flag2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SchematicBlockPipe that)) {
            return false;
        }
        return tileRotation == that.tileRotation &&
            Objects.equals(normalizeTileNbt(tileNbt), normalizeTileNbt(that.tileNbt));
    }

    @Override
    public int hashCode() {
        return Objects.hash(normalizeTileNbt(tileNbt), tileRotation);
    }

    private static CompoundTag normalizeTileNbt(CompoundTag tag) {
        CompoundTag copy = tag == null ? new CompoundTag() : tag.copy();
        copy.remove("x");
        copy.remove("y");
        copy.remove("z");
        return copy;
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
