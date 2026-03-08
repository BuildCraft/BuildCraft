/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.stream.Collectors;

public class Blueprint extends Snapshot {
    public final List<ISchematicBlock> palette = new ArrayList<>();
    public int[] data;
    public final List<ISchematicEntity> entities = new ArrayList<>();

    @Override
    public Blueprint copy() {
        Blueprint blueprint = new Blueprint();
        blueprint.size = size;
        blueprint.facing = facing;
        blueprint.offset = offset;
        blueprint.palette.addAll(palette);
        blueprint.data = data.clone();
        blueprint.entities.addAll(entities);
        blueprint.computeKey();
        return blueprint;
    }

    public void replace(ISchematicBlock from, ISchematicBlock to) {
        Collections.replaceAll(palette, from, to);
        // TODO: reallocate IDs
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.put("palette", NBTUtilBC.writeCompoundList(palette.stream().map(SchematicBlockManager::writeToNBT)));
        ListNBT list = new ListNBT();
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
//                    list.appendTag(new IntTag(data[posToIndex(x, y, z)]));
                    list.add(IntNBT.valueOf(data[posToIndex(x, y, z)]));
                }
            }
        }
        nbt.put("data", list);
        nbt.put("entities", NBTUtilBC.writeCompoundList(entities.stream().map(SchematicEntityManager::writeToNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        palette.clear();
        for (CompoundNBT schematicBlockTag :
                NBTUtilBC.readCompoundList(nbt.get("palette")).collect(Collectors.toList())) {
            // TODO: Allow reading blueprints partially - invalid elements should be replaced with air
            // (Although this needs to add a "pass-through" ISchematicBlock that will store the
            // invalid CompoundNBT and show up in the tooltip as an error, so that we can migrate
            // schematics through mod additions/deletions)
            palette.add(SchematicBlockManager.readFromNBT(schematicBlockTag));
        }
        data = new int[Snapshot.getDataSize(size)];
        ListNBT serializedDataList = nbt.contains("data", Constants.NBT.TAG_LIST)
                ? nbt.getList("data", Constants.NBT.TAG_INT)
                : null;
        int[] serializedDataIntArray = nbt.contains("data", Constants.NBT.TAG_INT_ARRAY)
                ? nbt.getIntArray("data")
                : null;
        if (serializedDataIntArray == null && serializedDataList == null) {
            throw new InvalidInputDataException("Can't read a blueprint with no data!");
        }
        int serializedDataLength = serializedDataList == null
                ? serializedDataIntArray.length
                : serializedDataList.size();
        if (serializedDataLength != getDataSize()) {
            throw new InvalidInputDataException(
                    "Serialized data has length of " + serializedDataLength +
                            ", but we expected " +
                            getDataSize() + " (" + size.toString() + ")"
            );
        }
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[posToIndex(x, y, z)] = serializedDataList == null
                            ? serializedDataIntArray[posToIndex(x, y, z)]
                            : serializedDataList.getInt(posToIndex(x, y, z));
                }
            }
        }
        for (CompoundNBT schematicEntityTag :
                NBTUtilBC.readCompoundList(nbt.get("entities")).collect(Collectors.toList())) {
            entities.add(SchematicEntityManager.readFromNBT(schematicEntityTag));
        }
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    @SuppressWarnings("WeakerAccess")
    public class BuildingInfo extends Snapshot.BuildingInfo {
        public final List<ItemStack>[] toPlaceRequiredItems;
        public final List<FluidStack>[] toPlaceRequiredFluids;
        public final List<ISchematicBlock> rotatedPalette;
        public final Set<ISchematicEntity> entities;
        public final Map<ISchematicEntity, List<ItemStack>> entitiesRequiredItems;
        public final Map<ISchematicEntity, List<FluidStack>> entitiesRequiredFluids;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            super(basePos, rotation);
            // noinspection unchecked
            toPlaceRequiredItems = (List<ItemStack>[]) new List<?>[getDataSize()];
            // noinspection unchecked
            toPlaceRequiredFluids = (List<FluidStack>[]) new List<?>[getDataSize()];
            rotatedPalette = ImmutableList.copyOf(
                    palette.stream()
                            .map(schematicBlock -> schematicBlock.getRotated(rotation))
                            .collect(Collectors.toList())
            );
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock schematicBlock = rotatedPalette.get(data[posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            toPlaceRequiredItems[posToIndex(x, y, z)] = schematicBlock.computeRequiredItems();
                            toPlaceRequiredFluids[posToIndex(x, y, z)] = schematicBlock.computeRequiredFluids();
                        }
                    }
                }
            }
            ImmutableSet.Builder<ISchematicEntity> entitiesBuilder = ImmutableSet.builder();
            ImmutableMap.Builder<ISchematicEntity, List<ItemStack>> entitiesRequiredItemsBuilder =
                    ImmutableMap.builder();
            ImmutableMap.Builder<ISchematicEntity, List<FluidStack>> entitiesRequiredFluidsBuilder =
                    ImmutableMap.builder();
            for (ISchematicEntity schematicEntity : getSnapshot().entities) {
                ISchematicEntity rotatedSchematicEntity = schematicEntity.getRotated(rotation);
                entitiesBuilder.add(rotatedSchematicEntity);
                entitiesRequiredItemsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredItems());
                entitiesRequiredFluidsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredFluids());
            }
            entities = entitiesBuilder.build();
            entitiesRequiredItems = entitiesRequiredItemsBuilder.build();
            entitiesRequiredFluids = entitiesRequiredFluidsBuilder.build();
        }

        @Override
        public Blueprint getSnapshot() {
            return Blueprint.this;
        }
    }
}
