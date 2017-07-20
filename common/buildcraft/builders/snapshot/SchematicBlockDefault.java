/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.InvalidInputDataException;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchematicBlockDefault implements ISchematicBlock<SchematicBlockDefault> {
    private final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    private IBlockState blockState;
    private final List<IProperty<?>> ignoredProperties = new ArrayList<>();
    private NBTTagCompound tileNbt;
    private final List<String> ignoredTags = new ArrayList<>();
    private Rotation tileRotation = Rotation.NONE;
    private Block placeBlock;
    private final Set<BlockPos> updateBlockOffsets = new HashSet<>();
    private final Set<Block> canBeReplacedWithBlocks = new HashSet<>();

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        if (context.blockState.getBlock().isAir(context.blockState, null, null)) {
            return false;
        }
        ResourceLocation registryName = context.block.getRegistryName();
        // noinspection ConstantConditions
        return registryName != null &&
            RulesLoader.READ_DOMAINS.contains(registryName.getResourceDomain()) &&
            RulesLoader.getRules(
                context.blockState,
                context.block.hasTileEntity(context.blockState) && context.world.getTileEntity(context.pos) != null
                    ? context.world.getTileEntity(context.pos).serializeNBT()
                    : null
            ).stream()
                .noneMatch(rule -> rule.ignore);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setRequiredBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredBlockOffsets.clear();
        rules.stream()
            .map(rule -> rule.requiredBlockOffsets)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .forEach(requiredBlockOffsets::add);
        if (context.block instanceof BlockFalling) {
            requiredBlockOffsets.add(new BlockPos(0, -1, 0));
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setBlockState(SchematicBlockContext context, Set<JsonRule> rules) {
        blockState = context.blockState;
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setIgnoredProperties(SchematicBlockContext context, Set<JsonRule> rules) {
        ignoredProperties.clear();
        rules.stream()
            .map(rule -> rule.ignoredProperties)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .flatMap(propertyName ->
                context.blockState.getProperties().keySet().stream()
                    .filter(property -> property.getName().equals(propertyName))
            )
            .forEach(ignoredProperties::add);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = null;
        if (context.block.hasTileEntity(context.blockState)) {
            TileEntity tileEntity = context.world.getTileEntity(context.pos);
            if (tileEntity != null) {
                tileNbt = tileEntity.serializeNBT();
            }
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setIgnoredTags(SchematicBlockContext context, Set<JsonRule> rules) {
        ignoredTags.clear();
        rules.stream()
            .map(rule -> rule.ignoredTags)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .forEach(ignoredTags::add);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setPlaceBlock(SchematicBlockContext context, Set<JsonRule> rules) {
        placeBlock = rules.stream()
            .map(rule -> rule.placeBlock)
            .filter(Objects::nonNull)
            .findFirst()
            .map(Block::getBlockFromName)
            .orElse(context.block);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setUpdateBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        updateBlockOffsets.clear();
        if (rules.stream().map(rule -> rule.updateBlockOffsets).anyMatch(Objects::nonNull)) {
            rules.stream()
                .map(rule -> rule.updateBlockOffsets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(updateBlockOffsets::add);
        } else {
            Stream.of(EnumFacing.VALUES)
                .map(EnumFacing::getDirectionVec)
                .map(BlockPos::new)
                .forEach(updateBlockOffsets::add);
            updateBlockOffsets.add(BlockPos.ORIGIN);
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setCanBeReplacedWithBlocks(SchematicBlockContext context, Set<JsonRule> rules) {
        canBeReplacedWithBlocks.clear();
        rules.stream()
            .map(rule -> rule.canBeReplacedWithBlocks)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(Block::getBlockFromName)
            .forEach(canBeReplacedWithBlocks::add);
        canBeReplacedWithBlocks.add(context.block);
        canBeReplacedWithBlocks.add(placeBlock);
    }

    @Override
    public void init(SchematicBlockContext context) {
        // noinspection ConstantConditions
        Set<JsonRule> rules = RulesLoader.getRules(
            context.blockState,
            context.block.hasTileEntity(context.blockState) && context.world.getTileEntity(context.pos) != null
                ? context.world.getTileEntity(context.pos).serializeNBT()
                : null
        );
        setRequiredBlockOffsets /*   */(context, rules);
        setBlockState /*             */(context, rules);
        setIgnoredProperties /*      */(context, rules);
        setTileNbt /*                */(context, rules);
        setIgnoredTags /*            */(context, rules);
        setPlaceBlock /*             */(context, rules);
        setUpdateBlockOffsets /*     */(context, rules);
        setCanBeReplacedWithBlocks /**/(context, rules);
    }

    @Override
    public boolean isAir() {
        return false;
    }

    @Nonnull
    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return requiredBlockOffsets;
    }

    @Nonnull
    @Override
    public List<ItemStack> computeRequiredItems(SchematicBlockContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        List<List<RequiredExtractor>> collect = rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return (
            collect.isEmpty()
                ? Stream.of(new RequiredExtractorItemFromBlock())
                : collect.stream().flatMap(Collection::stream)
        )
            .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromBlock(blockState, tileNbt).stream())
            .filter(((Predicate<ItemStack>) ItemStack::isEmpty).negate())
            .collect(Collectors.toList());
    }

    @Nonnull
    @Override
    public List<FluidStack> computeRequiredFluids(SchematicBlockContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromBlock(blockState, tileNbt).stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault schematicBlock = new SchematicBlockDefault();
        requiredBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.requiredBlockOffsets::add);
        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.ignoredProperties.addAll(ignoredProperties);
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.ignoredTags.addAll(ignoredTags);
        schematicBlock.tileRotation = tileRotation.add(rotation);
        schematicBlock.placeBlock = placeBlock;
        updateBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.updateBlockOffsets::add);
        schematicBlock.canBeReplacedWithBlocks.addAll(canBeReplacedWithBlocks);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isAirBlock(blockPos);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean build(World world, BlockPos blockPos) {
        if (placeBlock == Blocks.AIR) {
            return true;
        }
        IBlockState newBlockState = blockState;
        if (placeBlock != blockState.getBlock()) {
            newBlockState = placeBlock.getDefaultState();
            for (IProperty<?> property : blockState.getPropertyKeys()) {
                if (newBlockState.getPropertyKeys().contains(property)) {
                    newBlockState = BlockUtil.copyProperty(
                        property,
                        newBlockState,
                        blockState
                    );
                }
            }
        }
        for (IProperty<?> property : ignoredProperties) {
            newBlockState = BlockUtil.copyProperty(
                property,
                newBlockState,
                placeBlock.getDefaultState()
            );
        }
        if (world.setBlockState(blockPos, newBlockState, 11)) {
            updateBlockOffsets.stream()
                .map(blockPos::add)
                .forEach(updatePos -> world.notifyNeighborsOfStateChange(updatePos, placeBlock, false));
            if (tileNbt != null && blockState.getBlock().hasTileEntity(blockState)) {
                NBTTagCompound newTileNbt = new NBTTagCompound();
                tileNbt.getKeySet().stream()
                    .map(key -> Pair.of(key, tileNbt.getTag(key)))
                    .forEach(kv -> newTileNbt.setTag(kv.getKey(), kv.getValue()));
                newTileNbt.setInteger("x", blockPos.getX());
                newTileNbt.setInteger("y", blockPos.getY());
                newTileNbt.setInteger("z", blockPos.getZ());
                ignoredTags.stream()
                    .filter(newTileNbt::hasKey)
                    .forEach(newTileNbt::removeTag);
                TileEntity tileEntity = TileEntity.create(world, newTileNbt);
                if (tileEntity != null) {
                    tileEntity.setWorld(world);
                    world.setTileEntity(blockPos, tileEntity);
                    if (tileRotation != Rotation.NONE) {
                        tileEntity.rotate(tileRotation);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean buildWithoutChecks(World world, BlockPos blockPos) {
        if (world.setBlockState(blockPos, blockState, 0)) {
            if (tileNbt != null && blockState.getBlock().hasTileEntity(blockState)) {
                NBTTagCompound newTileNbt = new NBTTagCompound();
                tileNbt.getKeySet().stream()
                    .map(key -> Pair.of(key, tileNbt.getTag(key)))
                    .forEach(kv -> newTileNbt.setTag(kv.getKey(), kv.getValue()));
                newTileNbt.setInteger("x", blockPos.getX());
                newTileNbt.setInteger("y", blockPos.getY());
                newTileNbt.setInteger("z", blockPos.getZ());
                TileEntity tileEntity = TileEntity.create(world, newTileNbt);
                if (tileEntity != null) {
                    tileEntity.setWorld(world);
                    world.setTileEntity(blockPos, tileEntity);
                    if (tileRotation != Rotation.NONE) {
                        tileEntity.rotate(tileRotation);
                    }
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
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag(
            "requiredBlockOffsets",
            NBTUtilBC.writeCompoundList(
                requiredBlockOffsets.stream()
                    .map(NBTUtil::createPosTag)
            )
        );
        nbt.setTag("blockState", NBTUtil.writeBlockState(new NBTTagCompound(), blockState));
        nbt.setTag(
            "ignoredProperties",
            NBTUtilBC.writeStringList(
                ignoredProperties.stream()
                    .map(IProperty::getName)
            )
        );
        if (tileNbt != null) {
            nbt.setTag("tileNbt", tileNbt);
        }
        nbt.setTag("ignoredTags", NBTUtilBC.writeStringList(ignoredTags.stream()));
        nbt.setTag("tileRotation", NBTUtilBC.writeEnum(tileRotation));
        nbt.setString("placeBlock", Block.REGISTRY.getNameForObject(placeBlock).toString());
        nbt.setTag(
            "updateBlockOffsets",
            NBTUtilBC.writeCompoundList(
                updateBlockOffsets.stream()
                    .map(NBTUtil::createPosTag)
            )
        );
        nbt.setTag(
            "canBeReplacedWithBlocks",
            NBTUtilBC.writeStringList(
                canBeReplacedWithBlocks.stream()
                    .map(Block.REGISTRY::getNameForObject)
                    .map(Object::toString)
            )
        );
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) throws InvalidInputDataException {
        NBTUtilBC.readCompoundList(nbt.getTag("requiredBlockOffsets"))
            .map(NBTUtil::getPosFromTag)
            .forEach(requiredBlockOffsets::add);
        blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
        NBTUtilBC.readStringList(nbt.getTag("ignoredProperties"))
            .map(propertyName ->
                blockState.getPropertyKeys().stream()
                    .filter(property -> property.getName().equals(propertyName))
                    .findFirst()
                    .orElse(null)
            )
            .forEach(ignoredProperties::add);
        if (nbt.hasKey("tileNbt")) {
            tileNbt = nbt.getCompoundTag("tileNbt");
        }
        NBTUtilBC.readStringList(nbt.getTag("ignoredTags")).forEach(ignoredTags::add);
        tileRotation = NBTUtilBC.readEnum(nbt.getTag("tileRotation"), Rotation.class);
        placeBlock = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("placeBlock")));
        NBTUtilBC.readCompoundList(nbt.getTag("updateBlockOffsets"))
            .map(NBTUtil::getPosFromTag)
            .forEach(updateBlockOffsets::add);
        NBTUtilBC.readStringList(nbt.getTag("canBeReplacedWithBlocks"))
            .map(ResourceLocation::new)
            .map(Block.REGISTRY::getObject)
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
            ignoredTags.equals(that.ignoredTags) &&
            tileRotation == that.tileRotation &&
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
        result = 31 * result + ignoredTags.hashCode();
        result = 31 * result + tileRotation.hashCode();
        result = 31 * result + placeBlock.hashCode();
        result = 31 * result + updateBlockOffsets.hashCode();
        result = 31 * result + canBeReplacedWithBlocks.hashCode();
        return result;
    }
}
