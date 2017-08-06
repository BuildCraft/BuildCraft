/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;

import buildcraft.lib.misc.NBTUtilBC;

public class Blueprint extends Snapshot {
    public final List<ISchematicBlock<?>> palette = new ArrayList<>();
    public int[] data;
    public final List<ISchematicEntity<?>> entities = new ArrayList<>();

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

    public void replace(ISchematicBlock<?> from, ISchematicBlock<?> to) {
        Collections.replaceAll(palette, from, to);
        // TODO: reallocate IDs
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("palette", NBTUtilBC.writeCompoundList(palette.stream().map(SchematicBlockManager::writeToNBT)));
        NBTTagList list = new NBTTagList();
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    list.appendTag(new NBTTagInt(data[posToIndex(x, y, z)]));
                }
            }
        }
        nbt.setTag("data", list);
        nbt.setTag("entities", NBTUtilBC.writeCompoundList(entities.stream().map(SchematicEntityManager::writeToNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        palette.clear();
        for (NBTTagCompound schematicBlockTag :
            NBTUtilBC.readCompoundList(nbt.getTag("palette")).collect(Collectors.toList())) {
            // TODO: Allow reading blueprints partially - invalid elements should be replaced with air
            // (Although this needs to add a "pass-through" ISchematicBlock that will store the
            // invalid NBTTagCompound and show up in the tooltip as an error, so that we can migrate
            // schematics through mod additions/deletions)
            palette.add(SchematicBlockManager.readFromNBT(schematicBlockTag));
        }
        data = new int[size.getX() * size.getY() * size.getZ()];
        NBTTagList serializedDataList = nbt.hasKey("data", Constants.NBT.TAG_LIST)
            ? nbt.getTagList("data", Constants.NBT.TAG_INT)
            : null;
        int[] serializedDataIntArray = nbt.hasKey("data", Constants.NBT.TAG_INT_ARRAY)
            ? nbt.getIntArray("data")
            : null;
        if (serializedDataIntArray == null && serializedDataList == null) {
            throw new InvalidInputDataException("Can't read a blueprint with no data!");
        }
        int serializedDataLength = serializedDataList == null
            ? serializedDataIntArray.length
            : serializedDataList.tagCount();
        if (serializedDataLength != size.getX() * size.getY() * size.getZ()) {
            throw new InvalidInputDataException(
                "Serialized data has length of " + serializedDataLength +
                    ", but we expected " +
                    size.getX() * size.getY() * size.getZ() + " (" + size.toString() + ")"
            );
        }
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[posToIndex(x, y, z)] = serializedDataList == null
                        ? serializedDataIntArray[posToIndex(x, y, z)]
                        : serializedDataList.getIntAt(posToIndex(x, y, z));
                }
            }
        }
        for (NBTTagCompound schematicEntityTag :
            NBTUtilBC.readCompoundList(nbt.getTag("entities")).collect(Collectors.toList())) {
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
        public final Set<ISchematicEntity<?>> entities = new HashSet<>();
        public final Map<ISchematicEntity<?>, List<ItemStack>> entitiesRequiredItems = new HashMap<>();
        public final Map<ISchematicEntity<?>, List<FluidStack>> entitiesRequiredFluids = new HashMap<>();

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            super(basePos, rotation);
            // noinspection unchecked
            toPlaceRequiredItems = (List<ItemStack>[]) new List<?>[size.getX() * size.getY() * size.getZ()];
            // noinspection unchecked
            toPlaceRequiredFluids = (List<FluidStack>[]) new List<?>[size.getX() * size.getY() * size.getZ()];
            Pair<List<ItemStack>[][][], List<FluidStack>[][][]> required =
                SchematicBlockManager.computeRequired(getSnapshot());
            Pair<List<List<ItemStack>>, List<List<FluidStack>>> requiredEntities =
                SchematicEntityManager.computeRequired(getSnapshot());
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock<?> schematicBlock = palette.get(data[posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            toPlaceRequiredItems[posToIndex(x, y, z)] = required.getLeft()[x][y][z];
                            toPlaceRequiredFluids[posToIndex(x, y, z)] = required.getRight()[x][y][z];
                        }
                    }
                }
            }
            int i = 0;
            for (ISchematicEntity<?> schematicEntity : getSnapshot().entities) {
                ISchematicEntity<?> rotatedSchematicEntity = schematicEntity.getRotated(rotation);
                entities.add(rotatedSchematicEntity);
                entitiesRequiredItems.put(rotatedSchematicEntity, requiredEntities.getLeft().get(i));
                entitiesRequiredFluids.put(rotatedSchematicEntity, requiredEntities.getRight().get(i));
                i++;
            }
        }

        @Override
        public Blueprint getSnapshot() {
            return Blueprint.this;
        }
    }
}
