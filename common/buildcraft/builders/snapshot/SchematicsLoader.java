package buildcraft.builders.snapshot;

import buildcraft.lib.misc.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum SchematicsLoader {
    INSTANCE;

    private Set<JsonRule> getRules(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block
    ) {
        return RulesLoader.INSTANCE.rules.stream()
                .filter(rule -> rule.selectors != null)
                .filter(rule ->
                        rule.selectors.stream()
                                .anyMatch(selector -> {
                                    boolean complex = selector.contains("[");
                                    return Block.getBlockFromName(
                                            complex
                                                    ? selector.substring(0, selector.indexOf("["))
                                                    : selector
                                    ) == block &&
                                            (!complex ||
                                                    Arrays.stream(
                                                            selector.substring(
                                                                    selector.indexOf("[") + 1,
                                                                    selector.indexOf("]")
                                                            )
                                                                    .split(",")
                                                    )
                                                            .map(nameValue -> nameValue.split("="))
                                                            .allMatch(nameValue ->
                                                                    blockState.getPropertyKeys().stream()
                                                                            .filter(property ->
                                                                                    property.getName().equals(nameValue[0])
                                                                            )
                                                                            .findFirst()
                                                                            .map(blockState::getValue)
                                                                            .map(Object::toString)
                                                                            .map(nameValue[1]::equals)
                                                                            .orElse(false)
                                                            )
                                            );
                                })
                )
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<BlockPos> getRequiredBlockOffsets(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        Set<BlockPos> requiredBlockOffsets = rules.stream()
                .filter(rule -> rule.requiredBlockOffsets != null)
                .map(rule -> rule.requiredBlockOffsets)
                .flatMap(poses -> poses.stream().map(ints -> new BlockPos(ints[0], ints[1], ints[2])))
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (block instanceof BlockFalling) {
            requiredBlockOffsets.add(new BlockPos(0, -1, 0));
        }
        rules.stream()
                .map(rule -> rule.copyOppositeRequiredBlockOffsetFromProperty)
                .forEach(propertyName ->
                        blockState.getProperties().keySet().stream()
                                .filter(property -> property.getName().equals(propertyName))
                                .map(property -> (PropertyDirection) property)
                                .map(blockState::getValue)
                                .map(EnumFacing::getOpposite)
                                .map(EnumFacing::getDirectionVec)
                                .map(BlockPos::new)
                                .forEach(requiredBlockOffsets::add)
                );
        if (rules.stream().anyMatch(rule -> rule.copyRequiredBlockOffsetsFromProperties)) {
            for (EnumFacing side : EnumFacing.values()) {
                if (blockState.getProperties().keySet().stream()
                        .filter(property -> property.getName().equals(side.getName()))
                        .map(property -> (PropertyBool) property)
                        .anyMatch(blockState::getValue)) {
                    requiredBlockOffsets.add(new BlockPos(side.getDirectionVec()));
                }
            }
        }
        return requiredBlockOffsets;
    }

    private List<IProperty<?>> getIgnoredProperties(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        return rules.stream()
                .map(rule -> rule.ignoredProperties)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .flatMap(propertyName ->
                        blockState.getProperties().keySet().stream()
                                .filter(property -> property.getName().equals(propertyName))
                )
                .collect(Collectors.toList());
    }

    private NBTTagCompound getTileNbt(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        NBTTagCompound tileNbt = null;
        if (block.hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(pos);
            if (tileEntity != null) {
                tileNbt = tileEntity.serializeNBT();
            }
        }
        return tileNbt;
    }

    private List<String> getIgnoredTags(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        return rules.stream()
                .map(rule -> rule.ignoredTags)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Block getPlaceBlock(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        return rules.stream()
                .map(rule -> rule.placeBlock)
                .filter(Objects::nonNull)
                .findFirst()
                .map(Block::getBlockFromName)
                .orElse(block);
    }

    private Set<Block> getCanBeReplacedWithBlocks(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        return rules.stream()
                .map(rule -> rule.canBeReplacedWithBlocks)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(Block::getBlockFromName)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private List<ItemStack> getRequiredItems(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        List<ItemStack> requiredItems = new ArrayList<>();
        requiredItems.add(
                block.getPickBlock(
                        blockState,
                        null,
                        world,
                        pos,
                        null
                )
        );
        if (rules.stream().anyMatch(rule -> rule.copyRequiredItemsFromDrops)) {
            requiredItems.clear();
            requiredItems.addAll(block.getDrops(
                    world,
                    pos,
                    blockState,
                    0
            ));
        }
        if (rules.stream().noneMatch(rule -> rule.doNotCopyRequiredItemsFromBreakBlockDrops)) {
            if (world instanceof FakeWorld) {
                requiredItems.addAll(((FakeWorld) world).breakBlockAndGetDrops(pos));
            }
        }
        if (rules.stream().filter(rule -> rule.requiredItems != null).count() > 0) {
            requiredItems.clear();
            rules.stream()
                    .filter(rule -> rule.requiredItems != null)
                    .map(rule -> rule.requiredItems)
                    .flatMap(itemNames ->
                            itemNames.stream()
                                    .map(itemName -> itemName.contains("@") ? itemName : itemName + "@0")
                                    .map(itemName ->
                                            new ItemStack(
                                                    Objects.requireNonNull(
                                                            Item.getByNameOrId(
                                                                    itemName.substring(
                                                                            0,
                                                                            itemName.indexOf("@")
                                                                    )
                                                            )
                                                    ),
                                                    1,
                                                    Integer.parseInt(itemName.substring(itemName.indexOf("@") + 1))
                                            )
                                    )
                    )
                    .filter(Objects::nonNull)
                    .forEach(requiredItems::add);
        }
        rules.stream()
                .map(rule -> rule.copyRequiredItemsCountFromProperty)
                .filter(Objects::nonNull)
                .forEach(propertyName ->
                        blockState.getProperties().keySet().stream()
                                .filter(property -> property.getName().equals(propertyName))
                                .map(property -> (PropertyInteger) property)
                                .map(blockState::getValue)
                                .findFirst()
                                .ifPresent(value -> requiredItems.forEach(stack -> stack.setCount(stack.getCount() * value)))
                );
        if (block.hasTileEntity(blockState)) {
            TileEntity tileEntity = world.getTileEntity(pos);
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
        return requiredItems;
    }

    private List<Fluid> getRequiredFluids(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block,
            Set<JsonRule> rules
    ) {
        List<Fluid> requiredFluids = new ArrayList<>();
        if (BlockUtil.getFluidWithFlowing(block) != null) {
            if (BlockUtil.getFluid(block) != null) {
                requiredFluids.add(BlockUtil.getFluid(block));
            } else {
                return null;
            }
        }
        return requiredFluids;
    }

    public SchematicBlock getSchematicBlock(
            World world,
            BlockPos basePos,
            BlockPos pos,
            IBlockState blockState,
            Block block
    ) {
        ResourceLocation registryName = block.getRegistryName();
        if (registryName == null) {
            return getIgnoredSchematicBlock(world, basePos, pos);
        }
        String domain = registryName.getResourceDomain();
        if (!RulesLoader.INSTANCE.readDomains.contains(domain)) {
            return getIgnoredSchematicBlock(world, basePos, pos);
        }
        // Get list of all rules for this block
        Set<JsonRule> rules = getRules(world, basePos, pos, blockState, block);
        // -- requiredBlockOffsets --
        Set<BlockPos> requiredBlockOffsets = getRequiredBlockOffsets(world, basePos, pos, blockState, block, rules);
        if (getRequiredFluids(world, basePos, pos, blockState, block, rules) == null) {
            return getIgnoredSchematicBlock(world, basePos, pos);
        }
        // -- ignoredProperties --
        List<IProperty<?>> ignoredProperties = getIgnoredProperties(world, basePos, pos, blockState, block, rules);
        // -- tileNbt --
        NBTTagCompound tileNbt = getTileNbt(world, basePos, pos, blockState, block, rules);
        // -- tileNbt --
        List<String> ignoredTags = getIgnoredTags(world, basePos, pos, blockState, block, rules);
        // -- placeBlock --
        Block placeBlock = getPlaceBlock(world, basePos, pos, blockState, block, rules);
        // -- canBeReplacedWithBlocks --
        Set<Block> canBeReplacedWithBlocks = getCanBeReplacedWithBlocks(world, basePos, pos, blockState, block, rules);
        canBeReplacedWithBlocks.add(block);
        canBeReplacedWithBlocks.add(placeBlock);
        // Form schematic block
        if (rules.stream().anyMatch(rule -> rule.ignore)) {
            return getIgnoredSchematicBlock(world, basePos, pos);
        }
        return new SchematicBlock(
                requiredBlockOffsets,
                blockState,
                ignoredProperties,
                tileNbt,
                ignoredTags,
                placeBlock,
                canBeReplacedWithBlocks
        );
    }

    public SchematicBlock getIgnoredSchematicBlock(World world, BlockPos basePos, BlockPos pos) {
        return getSchematicBlock(world, basePos, pos, Blocks.AIR.getDefaultState(), Blocks.AIR);
    }

    private void computeRequiredForPos(FakeWorld world, Blueprint blueprint, BlockPos pos) {
        BlockPos basePos = FakeWorld.BLUEPRINT_OFFSET;
        SchematicBlock schematicBlock = blueprint.data
                [pos.getX() - basePos.getX()]
                [pos.getY() - basePos.getY()]
                [pos.getZ() - basePos.getZ()];
        IBlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();
        Set<JsonRule> rules = getRules(world, basePos, pos, blockState, block);
        schematicBlock.requiredItems = getRequiredItems(world, basePos, pos, blockState, block, rules);
        schematicBlock.requiredFluids = getRequiredFluids(world, basePos, pos, blockState, block, rules);
        if (schematicBlock.requiredFluids == null) {
            blueprint.data[pos.getX()][pos.getY()][pos.getZ()] = getIgnoredSchematicBlock(world, basePos, pos);
        }
    }

    public void computeRequired(Blueprint blueprint) {
        FakeWorld world = new FakeWorld();
        for (int z = 0; z < blueprint.size.getZ(); z++) {
            for (int y = 0; y < blueprint.size.getY(); y++) {
                for (int x = 0; x < blueprint.size.getX(); x++) {
                    world.uploadBlueprint(blueprint);
                    computeRequiredForPos(world, blueprint, new BlockPos(x, y, z).add(FakeWorld.BLUEPRINT_OFFSET));
                    world.clear();
                }
            }
        }
    }
}
