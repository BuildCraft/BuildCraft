/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import buildcraft.lib.dimension.BlueprintCalculator;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.WorkerThreadUtil;
import buildcraft.lib.misc.data.Box;

public class Blueprint extends Snapshot {
    public final List<ISchematicBlock<?>> palette = new ArrayList<>();
    public int[][][] data;
    public final List<ISchematicEntity<?>> entities = new ArrayList<>();

    public Blueprint copy() {
        Blueprint blueprint = new Blueprint();
        blueprint.size = size;
        blueprint.facing = facing;
        blueprint.offset = offset;
        blueprint.palette.addAll(palette);
        blueprint.data = new int[size.getX()][size.getY()][size.getZ()];
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    blueprint.data[x][y][z] = data[x][y][z];
                }
            }
        }
        blueprint.entities.addAll(entities);
        blueprint.header = header.withHash(blueprint.computeHash());
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
                    list.appendTag(new NBTTagInt(data[x][y][z]));
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
        for (NBTTagCompound schematicBlockTag : NBTUtilBC.readCompoundList(nbt.getTagList("palette",
            Constants.NBT.TAG_COMPOUND)).collect(Collectors.toList())) {
            // TODO: Allow reading blueprints partially - invalid elements should be replaced with air
            // (Although this needs to add a "pass-through" ISchematicBlock that will store the
            // invalid NBTTagCompound and show up in the tooltip as an error, so that we can migrate
            // schematics through mod additions/deletions)
            palette.add(SchematicBlockManager.readFromNBT(schematicBlockTag));
        }
        data = new int[size.getX()][size.getY()][size.getZ()];

        NBTTagList list = nbt.hasKey("data", Constants.NBT.TAG_LIST) ? nbt.getTagList("data", Constants.NBT.TAG_INT)
            : null;
        int[] serializedData = nbt.hasKey("data", Constants.NBT.TAG_INT_ARRAY) ? nbt.getIntArray("data") : new int[0];

        if (serializedData.length == 0) {
            throw new InvalidInputDataException("Can't read a blueprint with no data!");
        }
        int len = list == null ? serializedData.length : list.tagCount();
        if (len != size.getX() * size.getY() * size.getZ()) {
            throw new InvalidInputDataException("Pallette has length of " + len
                + ", but we expected " + size.getX() * size.getY() * size.getZ() + size.toString());
        }
        int i = 0;
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    data[x][y][z] = list == null ? serializedData[i] : list.getIntAt(i);
                    i++;
                }
            }
        }
        for (NBTTagCompound schematicEntityTag : NBTUtilBC.readCompoundList(nbt.getTagList("entities",
            Constants.NBT.TAG_COMPOUND)).collect(Collectors.toList())) {
            entities.add(SchematicEntityManager.readFromNBT(schematicEntityTag));
        }
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    public class BuildingInfo {
        public final BlockPos basePos;
        public final Rotation rotation;
        public final Box box;

        public final List<BlockPos> toBreak = new ArrayList<>();
        public final Map<BlockPos, ISchematicBlock<?>> toPlace = new HashMap<>();
        public final Map<BlockPos, List<ItemStack>> toPlaceRequiredItems = new HashMap<>();
        public final Map<BlockPos, List<FluidStack>> toPlaceRequiredFluids = new HashMap<>();
        public final List<ISchematicEntity<?>> entities = new ArrayList<>();
        public final Map<ISchematicEntity<?>, List<ItemStack>> entitiesRequiredItems = new HashMap<>();
        public final Map<ISchematicEntity<?>, List<FluidStack>> entitiesRequiredFluids = new HashMap<>();

        private boolean finishedComputing = false;
        private final Future<BlueprintCalculator.BuildingInfoData> future;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            this.basePos = basePos;
            this.rotation = rotation;
            box = new Box();
            future = WorkerThreadUtil.executeWorkTask(new BlueprintCalculator(Blueprint.this), false);
        }

        private void processData() {
            BlueprintCalculator.BuildingInfoData infoData = null;
            try {
                infoData = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                throw new RuntimeException("Something went wrong during blueprint requirement calculations!", e);
            }
            List<ItemStack>[][][] blockRequirementsItems = infoData.blockRequirementsItems;
            List<FluidStack>[][][] blockRequirmentsFluids = infoData.blockRequirementsFluids;
            List<List<ItemStack>> entityRequirementsItems = infoData.entityRequirementsItems;
            List<List<FluidStack>> entityRequirementsFluids = infoData.entityRequiremntsFluids;

            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock<?> schematicBlock = palette.get(Blueprint.this.data[x][y][z]);
                        BlockPos blockPos = new BlockPos(x, y, z).rotate(rotation).add(basePos).add(offset.rotate(
                                rotation));
                        if (schematicBlock.isAir()) {
                            toBreak.add(blockPos);
                        } else {
                            toPlace.put(blockPos, schematicBlock.getRotated(rotation));
                            toPlaceRequiredItems.put(blockPos, blockRequirementsItems[x][y][z]);
                            toPlaceRequiredFluids.put(blockPos, blockRequirmentsFluids[x][y][z]);
                        }
                    }
                }
            }
            int i = 0;
            for (ISchematicEntity<?> schematicEntity : getSnapshot().entities) {
                ISchematicEntity<?> rotatedSchematicEntity = schematicEntity.getRotated(rotation);
                entities.add(rotatedSchematicEntity);
                entitiesRequiredItems.put(rotatedSchematicEntity, entityRequirementsItems.get(i));
                entitiesRequiredFluids.put(rotatedSchematicEntity, entityRequirementsFluids.get(i));
                i++;
            }
            Stream.concat(toBreak.stream(), toPlace.keySet().stream()).forEach(box::extendToEncompass);

            finishedComputing = true;
        }

        public boolean hasFinishedComputing() {
            if (!finishedComputing && future.isDone()) {
                processData();
            }
            return finishedComputing;
        }

        public Blueprint getSnapshot() {
            return Blueprint.this;
        }

        public Box getBox() {
            return box;
        }
    }
}
