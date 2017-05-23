/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.enums.EnumSnapshotType;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.ISchematicEntity;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Blueprint extends Snapshot {
    public List<ISchematicBlock<?>> palette;
    public int[][][] data;
    public List<ISchematicEntity<?>> entities;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ITileForSnapshotBuilder> SnapshotBuilder<T> createBuilder(T tile) {
        return (SnapshotBuilder<T>) new BlueprintBuilder((ITileForBlueprintBuilder) tile);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = super.serializeNBT();
        nbt.setTag("palette", NBTUtilBC.writeCompoundList(palette.stream().map(SchematicBlockManager::writeToNBT)));
        int[] serializedData = new int[size.getX() * size.getY() * size.getZ()];
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    serializedData[i++] = data[x][y][z];
                }
            }
        }
        nbt.setIntArray("data", serializedData);
        nbt.setTag("entities", NBTUtilBC.writeCompoundList(entities.stream().map(SchematicEntityManager::writeToNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        palette = NBTUtilBC.readCompoundList(nbt.getTagList("palette", Constants.NBT.TAG_COMPOUND))
            .map(SchematicBlockManager::readFromNBT)
            .collect(Collectors.toList());
        data = new int[size.getX()][size.getY()][size.getZ()];
        int[] serializedData = nbt.getIntArray("data");
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[x][y][z] = serializedData[i++];
                }
            }
        }
        entities = NBTUtilBC.readCompoundList(nbt.getTagList("entities", Constants.NBT.TAG_COMPOUND))
            .map(SchematicEntityManager::readFromNBT)
            .collect(Collectors.toList());
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    public class BuildingInfo {
        public final BlockPos basePos;
        public final Rotation rotation;
        private final Box box;
        public final List<BlockPos> toBreak = new ArrayList<>();
        public final Map<BlockPos, ISchematicBlock<?>> toPlace = new HashMap<>();
        public final Map<BlockPos, List<ItemStack>> toPlaceRequiredItems = new HashMap<>();
        public final Map<BlockPos, List<FluidStack>> toPlaceRequiredFluids = new HashMap<>();
        public final List<ISchematicEntity<?>> entities = new ArrayList<>();
        public final Map<ISchematicEntity<?>, List<ItemStack>> entitiesRequiredItems = new HashMap<>();
        public final Map<ISchematicEntity<?>, List<FluidStack>> entitiesRequiredFluids = new HashMap<>();
        public final int maxLevel;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            Pair<List<ItemStack>[][][], List<FluidStack>[][][]> required =
                SchematicBlockManager.computeRequired(getSnapshot());
            Pair<List<List<ItemStack>>, List<List<FluidStack>>> requiredEntities =
                SchematicEntityManager.computeRequired(getSnapshot());
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock<?> schematicBlock = palette.get(data[x][y][z]);
                        BlockPos blockPos = new BlockPos(x, y, z).rotate(rotation)
                            .add(basePos)
                            .add(offset.rotate(rotation));
                        if (schematicBlock.isAir()) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.put(blockPos, schematicBlock.getRotated(rotation));
                            toPlaceRequiredItems.put(blockPos, required.getLeft()[x][y][z]);
                            toPlaceRequiredFluids.put(blockPos, required.getRight()[x][y][z]);
                        }
                    }
                }
            }
            int i = 0;
            for (ISchematicEntity<?> schematicEntity : getSnapshot().entities) {
                ISchematicEntity<?> rotatedSchematicEntity = schematicEntity.getRotated(rotation);
                entities.add(rotatedSchematicEntity);
                entitiesRequiredItems.put(rotatedSchematicEntity,  requiredEntities.getLeft().get(i));
                entitiesRequiredFluids.put(rotatedSchematicEntity,  requiredEntities.getRight().get(i));
                i++;
            }
            box = new Box();
            Stream.concat(toBreak.stream(), toPlace.keySet().stream()).forEach(box::extendToEncompass);
            maxLevel = toPlace.values().stream().mapToInt(ISchematicBlock::getLevel).max().orElse(0);
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
