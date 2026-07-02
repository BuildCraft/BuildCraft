/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import ct.buildcraft.api.core.InvalidInputDataException;
import ct.buildcraft.api.enums.EnumSnapshotType;
import ct.buildcraft.api.schematics.ISchematicBlock;
import ct.buildcraft.api.schematics.ISchematicEntity;
import ct.buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

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

    /**
     * Counts blueprint blocks whose palette entry matches {@code schematicBlock}. This counts placed blocks, not palette
     * rows, so a single palette entry used one hundred times returns 100.
     */
    public int countMatchingSchematic(ISchematicBlock schematicBlock) {
        if (schematicBlock == null || data == null || palette.isEmpty()) {
            return 0;
        }
        int matches = 0;
        for (int index : data) {
            if (index >= 0 && index < palette.size() && schematicMatchesForReplacement(palette.get(index), schematicBlock)) {
                matches++;
            }
        }
        return matches;
    }

    /**
     * Replaces every palette entry matching {@code from} with {@code to} and compacts the palette afterwards.
     *
     * @return the number of blueprint blocks whose schematic was changed.
     */
    public int replace(ISchematicBlock from, ISchematicBlock to) {
        if (from == null || to == null || data == null || palette.isEmpty() || schematicMatchesForReplacement(from, to)) {
            return 0;
        }

        int replacedBlocks = countMatchingSchematic(from);
        if (replacedBlocks <= 0) {
            return 0;
        }

        int[] remap = new int[palette.size()];
        List<ISchematicBlock> newPalette = new ArrayList<>();
        for (int oldIndex = 0; oldIndex < palette.size(); oldIndex++) {
            ISchematicBlock schematicBlock = palette.get(oldIndex);
            ISchematicBlock replacement = schematicMatchesForReplacement(schematicBlock, from) ? to : schematicBlock;
            int newIndex = findMatchingPaletteIndex(newPalette, replacement);
            if (newIndex < 0) {
                newIndex = newPalette.size();
                newPalette.add(replacement);
            }
            remap[oldIndex] = newIndex;
        }

        for (int i = 0; i < data.length; i++) {
            int oldIndex = data[i];
            if (oldIndex >= 0 && oldIndex < remap.length) {
                data[i] = remap[oldIndex];
            }
        }

        palette.clear();
        palette.addAll(newPalette);
        return replacedBlocks;
    }

    private static int findMatchingPaletteIndex(List<ISchematicBlock> palette, ISchematicBlock schematicBlock) {
        for (int i = 0; i < palette.size(); i++) {
            if (schematicMatchesForReplacement(palette.get(i), schematicBlock)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean schematicMatchesForReplacement(ISchematicBlock a, ISchematicBlock b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a == null || b == null || a.getClass() != b.getClass()) {
            return false;
        }
        CompoundTag tagA = normalizedSchematicTag(a);
        CompoundTag tagB = normalizedSchematicTag(b);
        return tagA.equals(tagB);
    }

    private static CompoundTag normalizedSchematicTag(ISchematicBlock schematicBlock) {
        CompoundTag tag = SchematicBlockManager.writeToNBT(schematicBlock).copy();
        normalizeSchematicTag(tag);
        return tag;
    }

    private static void normalizeSchematicTag(CompoundTag tag) {
        CompoundTag dataTag = tag.getCompound("data");
        if (dataTag.contains("tileNbt", Tag.TAG_COMPOUND)) {
            normalizeBlockEntityTag(dataTag.getCompound("tileNbt"));
        }
    }

    private static void normalizeBlockEntityTag(CompoundTag tag) {
        tag.remove("x");
        tag.remove("y");
        tag.remove("z");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        nbt.put("palette", NBTUtilBC.writeObjectList(palette.stream().map(SchematicBlockManager::writeToNBT)));
        ListTag list = new ListTag();
        for (int z = 0; z < size.getZ(); z++) {
            for (int y = 0; y < size.getY(); y++) {
                for (int x = 0; x < size.getX(); x++) {
                    list.add(IntTag.valueOf(data[posToIndex(x, y, z)]));
                }
            }
        }
        nbt.put("data", list);
        nbt.put("entities", NBTUtilBC.writeObjectList(entities.stream().map(SchematicEntityManager::writeToNBT)));
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        palette.clear();
        for (CompoundTag schematicBlockTag :
            NBTUtilBC.readCompoundList(nbt.get("palette")).collect(Collectors.toList())) {
            // TODO: Allow reading blueprints partially - invalid elements should be replaced with air
            // (Although this needs to add a "pass-through" ISchematicBlock that will store the
            // invalid CompoundTag and show up in the tooltip as an error, so that we can migrate
            // schematics through mod additions/deletions)
            palette.add(SchematicBlockManager.readFromNBT(schematicBlockTag));
        }
        data = new int[Snapshot.getDataSize(size)];
        ListTag serializedDataList = nbt.contains("data", Tag.TAG_LIST)
            ? nbt.getList("data", Tag.TAG_INT)
            : null;
        int[] serializedDataIntArray = nbt.contains("data", Tag.TAG_INT_ARRAY)
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
        for (CompoundTag schematicEntityTag :
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
        
        public final Level level;

        public BuildingInfo(BlockPos basePos, Rotation rotation, Level level) {
            super(basePos, rotation);
            this.level = level;
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
                            List<ItemStack> requiredItems = schematicBlock.computeRequiredItems(level);
							toPlaceRequiredItems[posToIndex(x, y, z)] = requiredItems;
							
/*                        	List<FluidStack> requiredFluids = new ArrayList<FluidStack>();
                        	requiredItems.stream().map(FluidUtil::getFluidHandler)
                        		.map(opt -> opt.lazyMap(fluidHandler -> fluidHandler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.EXECUTE)))
                        		.filter(LazyOptional::isPresent).map(opt -> opt.orElse(FluidStack.EMPTY)).forEach(requiredFluids::add);
                        	requiredFluids.addAll(schematicBlock.computeRequiredFluids(level));
                            toPlaceRequiredFluids[posToIndex(x, y, z)] = requiredFluids;*/
							toPlaceRequiredFluids[posToIndex(x, y, z)] = schematicBlock.computeRequiredFluids(level);
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
                entitiesRequiredItemsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredItems(level));
                entitiesRequiredFluidsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredFluids(level));
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
