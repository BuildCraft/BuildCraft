/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.IFakeWorld;
import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.NBTUtilBC;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.state.Property;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchematicBlockDefault implements ISchematicBlock {
    @SuppressWarnings("WeakerAccess")
    protected final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    @SuppressWarnings("WeakerAccess")
    protected BlockState blockState;
    @SuppressWarnings("WeakerAccess")
    protected final List<Property<?>> ignoredProperties = new ArrayList<>();
    @SuppressWarnings("WeakerAccess")
    protected CompoundNBT tileNbt;
    // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//    @SuppressWarnings("WeakerAccess")
//    protected Rotation tileRotation = Rotation.NONE;
    @SuppressWarnings("WeakerAccess")
    protected Block placeBlock;
    @SuppressWarnings("WeakerAccess")
    protected final Set<BlockPos> updateBlockOffsets = new HashSet<>();
    @SuppressWarnings("WeakerAccess")
    protected final Set<Block> canBeReplacedWithBlocks = new HashSet<>();
    // Calen
    private ListNBT items;
    private ListNBT fluids;

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
//        if (context.blockState.getBlock().isAir(context.blockState, null, null))
        if (context.blockState.isAir()) {
            return false;
        }
        ResourceLocation registryName = context.block.getRegistryName();
        // noinspection ConstantConditions
        return registryName != null &&
//                RulesLoader.READ_DOMAINS.contains(registryName.getResourceDomain()) &&
                RulesLoader.READ_DOMAINS.contains(registryName.getNamespace()) &&
                RulesLoader.getRules(
                                context.blockState,
                                context.block.hasTileEntity(context.blockState) &&
                                        context.world.getBlockEntity(context.pos) != null
                                        ? context.world.getBlockEntity(context.pos).serializeNBT()
                                        : null
                        ).stream()
                        .noneMatch(rule -> rule.ignore);
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setRequiredBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredBlockOffsets.clear();
        rules.stream()
                .map(rule -> rule.requiredBlockOffsets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(requiredBlockOffsets::add);
        if (context.block instanceof FallingBlock) {
            requiredBlockOffsets.add(new BlockPos(0, -1, 0));
        }
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setBlockState(SchematicBlockContext context, Set<JsonRule> rules) {
        blockState = context.blockState;
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setIgnoredProperties(SchematicBlockContext context, Set<JsonRule> rules) {
        ignoredProperties.clear();
        rules.stream()
                .map(rule -> rule.ignoredProperties)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .flatMap(propertyName ->
                        context.blockState.getProperties().stream()
                                .filter(property -> property.getName().equals(propertyName))
                )
                .forEach(ignoredProperties::add);
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = null;
        if (context.block.hasTileEntity(context.blockState)) {
            TileEntity tileEntity = context.world.getBlockEntity(context.pos);
            if (tileEntity != null) {
                tileNbt = tileEntity.serializeNBT();
                // Calen
                // containing items
                items = new ListNBT();
                tileEntity.getCapability(CapUtil.CAP_ITEMS).ifPresent(c ->
                {
                    for (int index = 0; index < c.getSlots(); index++) {
                        ItemStack stack = c.getStackInSlot(index);
                        if (!stack.isEmpty()) {
                            CompoundNBT itemNbt_i = new CompoundNBT();
                            stack.save(itemNbt_i);
                            items.add(itemNbt_i);
                        }
                    }
                });
            }
            // containing fluids
            fluids = new ListNBT();
            FluidUtil.getFluidHandler(context.world, context.pos, null).ifPresent(h ->
            {
                for (int index = 0; index < h.getTanks(); index++) {
                    FluidStack stack = h.getFluidInTank(index);
                    if (!stack.isEmpty()) {
                        CompoundNBT fluidNbt_i = new CompoundNBT();
                        stack.writeToNBT(fluidNbt_i);
                        fluids.add(fluidNbt_i);
                    }
                }
            });
        }
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setPlaceBlock(SchematicBlockContext context, Set<JsonRule> rules) {
        placeBlock = rules.stream()
                .map(rule -> rule.placeBlock)
                .filter(Objects::nonNull)
                .findFirst()
//                .map(Block::getBlockFromName)
                .map(BlockUtil::getBlockFromName)
                .orElse(context.block);
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setUpdateBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        updateBlockOffsets.clear();
        if (rules.stream().map(rule -> rule.updateBlockOffsets).anyMatch(Objects::nonNull)) {
            rules.stream()
                    .map(rule -> rule.updateBlockOffsets)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .forEach(updateBlockOffsets::add);
        } else {
            Stream.of(Direction.values())
//                    .map(Direction::getDirectionVec)
                    .map(Direction::getNormal)
                    .map(BlockPos::new)
                    .forEach(updateBlockOffsets::add);
            updateBlockOffsets.add(BlockPos.ZERO);
        }
    }

    @SuppressWarnings({ "unused", "WeakerAccess" })
    protected void setCanBeReplacedWithBlocks(SchematicBlockContext context, Set<JsonRule> rules) {
        canBeReplacedWithBlocks.clear();
        rules.stream()
                .map(rule -> rule.canBeReplacedWithBlocks)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
//                .map(Block::getBlockFromName)
                .map(BlockUtil::getBlockFromName)
                .forEach(canBeReplacedWithBlocks::add);
        canBeReplacedWithBlocks.add(context.block);
        canBeReplacedWithBlocks.add(placeBlock);
    }

    @Override
    public void init(SchematicBlockContext context) {
        // noinspection ConstantConditions
        Set<JsonRule> rules = RulesLoader.getRules(
                context.blockState,
                context.block.hasTileEntity(context.blockState) && context.world.getBlockEntity(context.pos) != null
                        ? context.world.getBlockEntity(context.pos).serializeNBT()
                        : null
        );
        setRequiredBlockOffsets /*   */(context, rules);
        setBlockState /*             */(context, rules);
        setIgnoredProperties /*      */(context, rules);
        setTileNbt /*                */(context, rules);
        setPlaceBlock /*             */(context, rules);
        setUpdateBlockOffsets /*     */(context, rules);
        setCanBeReplacedWithBlocks /**/(context, rules);
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return requiredBlockOffsets;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        List<List<RequiredExtractor>> collect = rules.stream()
                .map(rule -> rule.requiredExtractors)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
//        return (
//                collect.isEmpty()
//                        ? Stream.of(new RequiredExtractorItemFromBlock())
//                        : collect.stream().flatMap(Collection::stream)
//        )
//                .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromBlock(blockState, tileNbt).stream())
//                .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
//                .collect(Collectors.toList());

        // Calen: containing items
        List<ItemStack> ret = Lists.newArrayList();
        if (items != null) {
            for (int index = 0; index < items.size(); index++) {
                ItemStack stack_i = ItemStack.of(items.getCompound(index));
                if (!stack_i.isEmpty()) {
                    ret.add(stack_i);
                }
            }
        }
        ret.addAll(
                (
                        collect.isEmpty()
                                ? Stream.of(new RequiredExtractorItemFromBlock())
                                : collect.stream().flatMap(Collection::stream)
                )
                        .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromBlock(blockState, tileNbt).stream())
                        .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
                        .collect(Collectors.toList())
        );
        return ret;
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
//        return rules.stream()
//                .map(rule -> rule.requiredExtractors)
//                .filter(Objects::nonNull)
//                .flatMap(Collection::stream)
//                .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromBlock(blockState, tileNbt).stream())
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());

        // Calen: containing fluids
        List<FluidStack> ret = Lists.newArrayList();
        if (fluids != null) {
            for (int index = 0; index < fluids.size(); index++) {
                FluidStack stack_i = FluidStack.loadFluidStackFromNBT(fluids.getCompound(index));
                if (!stack_i.isEmpty()) {
                    ret.add(stack_i);
                }
            }
        }
        ret.addAll(
                rules.stream()
                        .map(rule -> rule.requiredExtractors)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromBlock(blockState, tileNbt).stream())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList())
        );
        return ret;
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault schematicBlock = SchematicBlockManager.createCleanCopy(this);
        requiredBlockOffsets.stream()
                .map(blockPos -> blockPos.rotate(rotation))
                .forEach(schematicBlock.requiredBlockOffsets::add);
//        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.blockState = blockState.rotate(rotation);
        schematicBlock.ignoredProperties.addAll(ignoredProperties);
//        schematicBlock.tileNbt = tileNbt;
        schematicBlock.tileNbt = tileNbt == null ? null : tileNbt.copy();
        schematicBlock.items = items == null ? null : items.copy();
        schematicBlock.fluids = fluids == null ? null : fluids.copy();
        // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
////        schematicBlock.tileRotation = tileRotation.add(rotation);
//        schematicBlock.tileRotation = tileRotation.getRotated(rotation);
        schematicBlock.placeBlock = placeBlock;
        updateBlockOffsets.stream()
                .map(blockPos -> blockPos.rotate(rotation))
                .forEach(schematicBlock.updateBlockOffsets::add);
        schematicBlock.canBeReplacedWithBlocks.addAll(canBeReplacedWithBlocks);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isEmptyBlock(blockPos);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean build(World world, BlockPos blockPos) {
        if (placeBlock == Blocks.AIR) {
            return true;
        }
        world.getProfiler().push("prepare block");
        BlockState newBlockState = blockState;
        if (placeBlock != blockState.getBlock()) {
            newBlockState = placeBlock.defaultBlockState();
            for (Property<?> property : blockState.getProperties()) {
                if (newBlockState.getProperties().contains(property)) {
                    newBlockState = BlockUtil.copyProperty(
                            property,
                            newBlockState,
                            blockState
                    );
                }
            }
        }
        for (Property<?> property : ignoredProperties) {
            newBlockState = BlockUtil.copyProperty(
                    property,
                    newBlockState,
                    placeBlock.defaultBlockState()
            );
        }
        world.getProfiler().pop();
        world.getProfiler().push("place block");
        boolean b = world.setBlock(blockPos, newBlockState, BlockConstants.UPDATE_ALL_IMMEDIATE);
        world.getProfiler().pop();
        if (b) {
            world.getProfiler().push("notify");
            updateBlockOffsets.stream()
                    .map(blockPos::offset)
//                    .forEach(updatePos -> world.notifyNeighborsOfStateChange(updatePos, placeBlock, false));
                    .forEach(updatePos -> world.updateNeighborsAt(updatePos, placeBlock));
            world.getProfiler().pop();
            if (tileNbt != null && blockState.getBlock().hasTileEntity(blockState)) {
                world.getProfiler().push("prepare tile");
                Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
                CompoundNBT replaceNbt = rules.stream()
                        .map(rule -> rule.replaceNbt)
                        .filter(Objects::nonNull)
                        .map(INBT.class::cast)
                        .reduce(NBTUtilBC::merge)
                        .map(CompoundNBT.class::cast)
                        .orElse(null);
                CompoundNBT newTileNbt = new CompoundNBT();
                tileNbt.getAllKeys().stream()
                        .map(key -> Pair.of(key, tileNbt.get(key)))
                        .forEach(kv -> newTileNbt.put(kv.getKey(), kv.getValue()));
                newTileNbt.putInt("x", blockPos.getX());
                newTileNbt.putInt("y", blockPos.getY());
                newTileNbt.putInt("z", blockPos.getZ());
                world.getProfiler().pop();
                world.getProfiler().push("place tile");
                TileEntity tileEntity = TileEntity.loadStatic(
                        blockState,
                        replaceNbt != null
                                ? (CompoundNBT) NBTUtilBC.merge(newTileNbt, replaceNbt)
                                : newTileNbt
                );
                if (tileEntity != null) {
                    // Calen: tileEntity#setLevel and tileEntity#clearRemoved will be called in world.setBlockEntity
//                    tileEntity.setLevel(world);
                    world.setBlockEntity(blockPos, tileEntity);
                    // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//                    if (tileRotation != Rotation.NONE) {
////                        tileEntity.rotate(tileRotation);
//                    }
                }
                world.getProfiler().pop();
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
//    public boolean buildWithoutChecks(World world, BlockPos blockPos)
    public boolean buildWithoutChecks(IFakeWorld world, BlockPos blockPos) {
        // Calen: if 0 -> FallingBlock will
        if (world.setBlock(blockPos, blockState, 0)) {
            if (tileNbt != null && blockState.getBlock().hasTileEntity(blockState)) {
                CompoundNBT newTileNbt = new CompoundNBT();
                tileNbt.getAllKeys().stream()
                        .map(key -> Pair.of(key, tileNbt.get(key)))
                        .forEach(kv -> newTileNbt.put(kv.getKey(), kv.getValue()));
                newTileNbt.putInt("x", blockPos.getX());
                newTileNbt.putInt("y", blockPos.getY());
                newTileNbt.putInt("z", blockPos.getZ());
//                TileEntity tileEntity = TileEntity.create(world, newTileNbt);
                TileEntity tileEntity = TileEntity.loadStatic(blockState, newTileNbt);
                if (tileEntity != null) {
                    // Calen: tileEntity#setLevel and tileEntity#clearRemoved will be called in world.setBlockEntity
//                    tileEntity.setLevel(world);
                    world.setBlockEntity(blockPos, tileEntity);
                    // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//                    if (tileRotation != Rotation.NONE)
////                    if (tileRotation != Rotation.NONE && tileEntity instanceof SkullBlockEntity skull) {
////                        tileEntity.rotate(tileRotation);
//                        world.getBlockState(blockPos).rotate(tileRotation);
//                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBuilt(World world, BlockPos blockPos) {
        return blockState != null &&
                canBeReplacedWithBlocks.contains(world.getBlockState(blockPos).getBlock()) &&
                BlockUtil.blockStatesWithoutBlockEqual(blockState, world.getBlockState(blockPos), ignoredProperties);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(
                "requiredBlockOffsets",
                NBTUtilBC.writeCompoundList(
                        requiredBlockOffsets.stream()
                                .map(NBTUtil::writeBlockPos)
                )
        );
//        nbt.put("blockState", NBTUtil.writeBlockState(new CompoundNBT(), blockState));
        nbt.put("blockState", NBTUtil.writeBlockState(blockState));
        nbt.put(
                "ignoredProperties",
                NBTUtilBC.writeStringList(
                        ignoredProperties.stream()
                                .map(Property::getName)
                )
        );
        if (tileNbt != null) {
            nbt.put("tileNbt", tileNbt);
        }
        // Calen: containing items & fluids
        if (items != null) {
            nbt.put("items", items);
        }
        if (fluids != null) {
            nbt.put("fluids", fluids);
        }

        // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//        nbt.put("tileRotation", NBTUtilBC.writeEnum(tileRotation));
//        nbt.putString("placeBlock", Block.REGISTRY.getNameForObject(placeBlock).toString());
        nbt.putString("placeBlock", ForgeRegistries.BLOCKS.getKey(placeBlock).toString());
        nbt.put(
                "updateBlockOffsets",
                NBTUtilBC.writeCompoundList(
                        updateBlockOffsets.stream()
                                .map(NBTUtil::writeBlockPos)
                )
        );
        nbt.put(
                "canBeReplacedWithBlocks",
                NBTUtilBC.writeStringList(
                        canBeReplacedWithBlocks.stream()
//                                .map(Block.REGISTRY::getNameForObject)
                                .map(ForgeRegistries.BLOCKS::getKey)
                                .map(Object::toString)
                )
        );
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) throws InvalidInputDataException {
        NBTUtilBC.readCompoundList(nbt.get("requiredBlockOffsets"))
                .map(NBTUtil::readBlockPos)
                .forEach(requiredBlockOffsets::add);
        blockState = NBTUtil.readBlockState(nbt.getCompound("blockState"));
        NBTUtilBC.readStringList(nbt.get("ignoredProperties"))
                .map(propertyName ->
                        blockState.getProperties().stream()
                                .filter(property -> property.getName().equals(propertyName))
                                .findFirst()
                                .orElse(null)
                )
                .forEach(ignoredProperties::add);
        if (nbt.contains("tileNbt")) {
            tileNbt = nbt.getCompound("tileNbt");
        }
        // Calen: containing items & fluids
        if (nbt.contains("items")) {
            items = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
        }
        if (nbt.contains("fluids")) {
            fluids = nbt.getList("fluids", Constants.NBT.TAG_COMPOUND);
        }

        // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//        tileRotation = NBTUtilBC.readEnum(nbt.get("tileRotation"), Rotation.class);
//        placeBlock = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("placeBlock")));
        placeBlock = BlockUtil.getBlockFromName(nbt.getString("placeBlock"));
        NBTUtilBC.readCompoundList(nbt.get("updateBlockOffsets"))
                .map(NBTUtil::readBlockPos)
                .forEach(updateBlockOffsets::add);
        NBTUtilBC.readStringList(nbt.get("canBeReplacedWithBlocks"))
                .map(ResourceLocation::new)
//                .map(Block.REGISTRY::getObject)
                .map(BlockUtil::getBlockFromName)
                .forEach(canBeReplacedWithBlocks::add);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SchematicBlockDefault that = (SchematicBlockDefault) o;

        return requiredBlockOffsets.equals(that.requiredBlockOffsets) &&
                blockState.equals(that.blockState) &&
                ignoredProperties.equals(that.ignoredProperties) &&
                (tileNbt != null ? tileNbt.equals(that.tileNbt) : that.tileNbt == null) &&
                // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//                tileRotation == that.tileRotation &&
                placeBlock.equals(that.placeBlock) &&
                updateBlockOffsets.equals(that.updateBlockOffsets) &&
                canBeReplacedWithBlocks.equals(that.canBeReplacedWithBlocks);
    }

    @Override
    public int hashCode() {
        int result = requiredBlockOffsets.hashCode();
        result = 31 * result + blockState.hashCode();
        result = 31 * result + ignoredProperties.hashCode();
        result = 31 * result + (tileNbt != null ? tileNbt.hashCode() : 0);
        // Calen: 1.12.2 tileRotation -> 1.18.2 SkullBlock BlockState ROTATION_16
//        result = 31 * result + tileRotation.hashCode();
        result = 31 * result + placeBlock.hashCode();
        result = 31 * result + updateBlockOffsets.hashCode();
        result = 31 * result + canBeReplacedWithBlocks.hashCode();
        return result;
    }
}
