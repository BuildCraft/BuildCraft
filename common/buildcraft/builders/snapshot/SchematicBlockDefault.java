package buildcraft.builders.snapshot;

import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.api.schematics.SchematicBlockContext;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
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
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.CapabilityItemHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SchematicBlockDefault implements ISchematicBlock<SchematicBlockDefault> {
    private int level;
    private final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    private IBlockState blockState;
    private final List<IProperty<?>> ignoredProperties = new ArrayList<>();
    private NBTTagCompound tileNbt;
    private final List<String> ignoredTags = new ArrayList<>();
    private Rotation tileRotation = Rotation.NONE;
    private Block placeBlock;
    private final Set<Block> canBeReplacedWithBlocks = new HashSet<>();
    private final List<ItemStack> requiredItems = new ArrayList<>();
    private final List<FluidStack> requiredFluids = new ArrayList<>();

    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        if (context.blockState.getBlock().isAir(context.blockState, null, null)) {
            return false;
        }
        ResourceLocation registryName = context.block.getRegistryName();
        if (registryName == null) {
            return false;
        }
        if (!RulesLoader.READ_DOMAINS.contains(registryName.getResourceDomain())) {
            return BlockUtil.getFluidWithFlowing(context.world, context.pos) != null;
        }
        return RulesLoader.getRules(context.blockState).stream().noneMatch(rule -> rule.ignore);
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setLevel(SchematicBlockContext context, Set<JsonRule> rules) {
        level = BlockUtil.getFluidWithFlowing(context.world, context.pos) != null ? 1 : 0;
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
        rules.stream()
                .map(rule -> rule.copyOppositeRequiredBlockOffsetFromProperty)
                .forEach(propertyName ->
                        context.blockState.getProperties().keySet().stream()
                                .filter(property -> property.getName().equals(propertyName))
                                .map(PropertyDirection.class::cast)
                                .map(context.blockState::getValue)
                                .map(EnumFacing::getOpposite)
                                .map(EnumFacing::getDirectionVec)
                                .map(BlockPos::new)
                                .forEach(requiredBlockOffsets::add)
                );
        if (rules.stream().anyMatch(rule -> rule.copyRequiredBlockOffsetsFromProperties)) {
            for (EnumFacing side : EnumFacing.values()) {
                if (context.blockState.getProperties().keySet().stream()
                        .filter(property -> property.getName().equals(side.getName()))
                        .map(PropertyBool.class::cast)
                        .anyMatch(context.blockState::getValue)) {
                    requiredBlockOffsets.add(new BlockPos(side.getDirectionVec()));
                }
            }
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
                .orElse(
                        BlockUtil.getFluidWithFlowing(context.world, context.pos) != null &&
                                BlockUtil.getFluid(context.world, context.pos) == null
                                ? Blocks.AIR
                                : context.block
                );
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

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setRequiredItems(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredItems.clear();
        requiredItems.add(
                context.block.getPickBlock(
                        context.blockState,
                        null,
                        context.world,
                        context.pos,
                        null
                )
        );
        if (rules.stream().anyMatch(rule -> rule.copyRequiredItemsFromDrops)) {
            requiredItems.clear();
            requiredItems.addAll(context.block.getDrops(
                    context.world,
                    context.pos,
                    context.blockState,
                    0
            ));
        }
        if (rules.stream().noneMatch(rule -> rule.doNotCopyRequiredItemsFromBreakBlockDrops)) {
            if (context.world instanceof FakeWorld) {
                requiredItems.addAll(((FakeWorld) context.world).breakBlockAndGetDrops(context.pos));
            }
        }
        if (rules.stream().map(rule -> rule.requiredItems).anyMatch(Objects::nonNull)) {
            requiredItems.clear();
            rules.stream()
                    .map(rule -> rule.requiredItems)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .forEach(requiredItems::add);
        }
        rules.stream()
                .map(rule -> rule.copyRequiredItemsCountFromProperty)
                .filter(Objects::nonNull)
                .forEach(propertyName ->
                        context.blockState.getProperties().keySet().stream()
                                .filter(property -> property.getName().equals(propertyName))
                                .map(PropertyInteger.class::cast)
                                .map(context.blockState::getValue)
                                .findFirst()
                                .ifPresent(value -> requiredItems.forEach(stack -> stack.setCount(stack.getCount() * value)))
                );
        if (context.block.hasTileEntity(context.blockState)) {
            TileEntity tileEntity = context.world.getTileEntity(context.pos);
            if (tileEntity != null) {
                rules.stream()
                        .map(rule -> rule.copyRequiredItemsFromItemHandlersOnSides)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(EnumFacing::byName)
                        .filter(side -> tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
                        .map(side -> tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
                        .filter(Objects::nonNull)
                        .flatMap(itemHandler ->
                                IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot)
                        )
                        .filter(stack -> !stack.isEmpty())
                        .forEach(requiredItems::add);
            }
        }
        requiredItems.removeIf(ItemStack::isEmpty);
        if (BlockUtil.getFluidWithFlowing(context.world, context.pos) != null) {
            requiredItems.clear();
        }
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    protected void setRequiredFluids(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredFluids.clear();
        if (BlockUtil.drainBlock(context.world, context.pos, false) != null) {
            requiredFluids.add(BlockUtil.drainBlock(context.world, context.pos, false));
        }
    }

    @Override
    public void init(SchematicBlockContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(context.blockState);
        setLevel /*                  */(context, rules);
        setRequiredBlockOffsets /*   */(context, rules);
        setBlockState /*             */(context, rules);
        setIgnoredProperties /*      */(context, rules);
        setTileNbt /*                */(context, rules);
        setIgnoredTags /*            */(context, rules);
        setPlaceBlock /*             */(context, rules);
        setCanBeReplacedWithBlocks /**/(context, rules);
        setRequiredItems /*          */(context, rules);
        setRequiredFluids /*         */(context, rules);
    }

    @Override
    public int getLevel() {
        return level;
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

    @Override
    public void computeRequiredItemsAndFluids(SchematicBlockContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(context.blockState);
        setRequiredItems(context, rules);
        setRequiredFluids(context, rules);
    }

    @Nonnull
    @Override
    public List<ItemStack> getRequiredItems() {
        return requiredItems;
    }

    @Nonnull
    @Override
    public List<FluidStack> getRequiredFluids() {
        return requiredFluids;
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault schematicBlock = new SchematicBlockDefault();
        schematicBlock.level = level;
        requiredBlockOffsets.stream()
                .map(blockPos -> blockPos.rotate(rotation))
                .forEach(schematicBlock.requiredBlockOffsets::add);
        schematicBlock.blockState = blockState.withRotation(rotation);
        schematicBlock.ignoredProperties.addAll(ignoredProperties);
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.ignoredTags.addAll(ignoredTags);
        schematicBlock.tileRotation = tileRotation.add(rotation);
        schematicBlock.placeBlock = placeBlock;
        schematicBlock.canBeReplacedWithBlocks.addAll(canBeReplacedWithBlocks);
        schematicBlock.requiredItems.addAll(requiredItems);
        schematicBlock.requiredFluids.addAll(requiredFluids);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(World world, BlockPos blockPos) {
        return world.isAirBlock(blockPos) ||
                BlockUtil.getFluidWithFlowing(blockState.getBlock()) != null &&
                        BlockUtil.getFluidWithFlowing(world, blockPos) != null &&
                        BlockUtil.getFluid(world, blockPos) == null;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean build(World world, BlockPos blockPos) {
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
            Stream.of(
                    Stream.of(EnumFacing.values()).map(blockPos::offset),
                    requiredBlockOffsets.stream().map(blockPos::add),
                    Stream.of(blockPos)
            )
                    .flatMap(Function.identity())
                    .distinct()
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
                blockState.getPropertyKeys().stream()
                        .filter(world.getBlockState(blockPos).getPropertyKeys()::contains)
                        .filter(property -> !ignoredProperties.contains(property))
                        .allMatch(property ->
                                Objects.equals(
                                        blockState.getValue(property),
                                        world.getBlockState(blockPos).getValue(property)
                                )
                        );
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("level", level);
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
    public void deserializeNBT(NBTTagCompound nbt) {
        level = nbt.getInteger("level");
        NBTUtilBC.readCompoundList(nbt.getTagList("requiredBlockOffsets", Constants.NBT.TAG_COMPOUND))
                .map(NBTUtil::getPosFromTag)
                .forEach(requiredBlockOffsets::add);
        blockState = NBTUtil.readBlockState(nbt.getCompoundTag("blockState"));
        NBTUtilBC.readStringList(nbt.getTagList("ignoredProperties", Constants.NBT.TAG_STRING))
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
        NBTUtilBC.readStringList(nbt.getTagList("ignoredTags", Constants.NBT.TAG_STRING)).forEach(ignoredTags::add);
        tileRotation = NBTUtilBC.readEnum(nbt.getTag("tileRotation"), Rotation.class);
        placeBlock = Block.REGISTRY.getObject(new ResourceLocation(nbt.getString("placeBlock")));
        NBTUtilBC.readStringList(nbt.getTagList("canBeReplacedWithBlocks", Constants.NBT.TAG_STRING))
                .map(ResourceLocation::new)
                .map(Block.REGISTRY::getObject)
                .forEach(canBeReplacedWithBlocks::add);
    }
}
