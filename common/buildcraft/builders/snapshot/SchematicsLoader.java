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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public enum SchematicsLoader {
    INSTANCE;

    public final Map<Block, Function<SchematicBlockContext, SchematicBlock>> schematicFactories = new HashMap<>();

    public void loadAll() {
        schematicFactories.clear();
        // For all blocks
        Block.REGISTRY.forEach(block -> {
            if (block == null || block.getRegistryName() == null) {
                return;
            }
            String domain = block.getRegistryName().getResourceDomain();
            // If mod of this block supports schematics
            if (RulesLoader.INSTANCE.readDomains.contains(domain)) {
                // Get list of all rules for this block
                Set<JsonRule> rules = RulesLoader.INSTANCE.rules.stream()
                        .filter(rule -> rule.blocks != null)
                        .filter(rule ->
                                rule.blocks.stream()
                                        .map(Block::getBlockFromName)
                                        .anyMatch(Predicate.isEqual(block))
                        )
                        .collect(Collectors.toCollection(HashSet::new));
                int oldSize = rules.size();
                // Add all parent rules to list
                while (true) {
                    new ArrayList<>(rules).stream() // Copy needed to avoid ConcurrentModificationException
                            .filter(rule -> rule.parentNames != null)
                            .flatMap(rule -> rule.parentNames.stream())
                            .flatMap(ruleName ->
                                    RulesLoader.INSTANCE.rules.stream()
                                            .filter(rule -> Objects.equals(rule.name, ruleName))
                            )
                            .forEach(rules::add);
                    if (oldSize == rules.size()) {
                        break;
                    } else {
                        oldSize = rules.size();
                    }
                }
                // Form items needed of placing this block
                List<ItemStack> requiredItems = new ArrayList<>();
                boolean getRequiredItemFromState;
//                boolean copyRequiredItemMetaFromBlock;
                if (rules.stream().filter(rule -> rule.requiredItems != null).count() > 0) {
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
//                    copyRequiredItemMetaFromBlock = false;
                    getRequiredItemFromState = false;
                } else {
//                    Item itemFromBlock = Item.getItemFromBlock(block);
//                    copyRequiredItemMetaFromBlock = rules.stream().anyMatch(rule -> rule.copyRequiredItemMetaFromBlock);
//                        requiredItems.add(new ItemStack(itemFromBlock));
                    getRequiredItemFromState = true;
                }
                Set<BlockPos> requiredBlockOffsets = rules.stream()
                        .filter(rule -> rule.requiredBlockOffsets != null)
                        .map(rule -> rule.requiredBlockOffsets)
                        .flatMap(poses -> poses.stream().map(ints -> new BlockPos(ints[0], ints[1], ints[2])))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));
                if (block instanceof BlockFalling) {
                    requiredBlockOffsets.add(new BlockPos(0, -1, 0));
                }
                Block placeBlock = rules.stream()
                        .map(rule -> rule.placeBlock)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .map(Block::getBlockFromName)
                        .orElse(block);
                Set<Block> canBeReplacedWithBlocks = rules.stream()
                        .map(rule -> rule.canBeReplacedWithBlocks)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .map(Block::getBlockFromName)
                        .collect(Collectors.toCollection(HashSet::new));
                canBeReplacedWithBlocks.add(block);
                canBeReplacedWithBlocks.add(placeBlock);
                boolean ignore = rules.stream().anyMatch(rule -> rule.ignore);
                List<Fluid> requiredFluids = new ArrayList<>();
                if (BlockUtil.getFluidWithFlowing(block) != null) {
                    if (BlockUtil.getFluid(block) != null) {
                        requiredFluids.add(BlockUtil.getFluid(block));
                    } else {
                        ignore = true;
                    }
                }
                // Add schematic generator
                schematicFactories.put(
                        block,
                        ignore ? schematicFactories.get(Blocks.AIR) : schematicBlockContext -> {
                            BlockPos relativePos = schematicBlockContext.pos.subtract(schematicBlockContext.basePos);
                            Set<BlockPos> currentRequiredBlockOffsets = new HashSet<>(requiredBlockOffsets);
                            List<ItemStack> currentRequiredItems = new ArrayList<>(requiredItems);
                            IBlockState blockState = schematicBlockContext.world.getBlockState(schematicBlockContext.pos);
                            NBTTagCompound tileNbt = null;
                            if (getRequiredItemFromState) {
                                currentRequiredItems.add(
                                        block.getPickBlock(
                                                blockState,
                                                null,
                                                schematicBlockContext.world,
                                                schematicBlockContext.pos,
                                                null
                                        )
                                );
                            }
//                            if (copyRequiredItemMetaFromBlock) {
//                                currentRequiredItems.set(
//                                        0,
//                                        new ItemStack(
//                                                requiredItems.get(0).getItem(),
//                                                requiredItems.get(0).getCount(),
//                                                blockState.getBlock().getMetaFromState(blockState)
//                                        )
//                                );
//                            }
                            if (rules.stream().anyMatch(rule -> rule.copyRequiredItemsFromDrops)) {
                                currentRequiredItems.clear();
                                currentRequiredItems.addAll(block.getDrops(
                                        schematicBlockContext.world,
                                        schematicBlockContext.pos,
                                        blockState,
                                        0
                                ));
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
                                                    .ifPresent(value -> {
                                                        for (int i = 0; i < currentRequiredItems.size(); i++) {
                                                            ItemStack stack = currentRequiredItems.get(i);
                                                            currentRequiredItems.set(
                                                                    i,
                                                                    new ItemStack(
                                                                            stack.getItem(),
                                                                            stack.getCount() * value,
                                                                            stack.getMetadata()
                                                                    )
                                                            );
                                                        }
                                                    })
                                    );
//                            if (currentRequiredItems.size() == 1) {
//                                rules.stream()
//                                        .map(rule -> rule.copyRequiredItemMetaFromProperty)
//                                        .filter(Objects::nonNull)
//                                        .forEach(propertyName ->
//                                                blockState.getProperties().keySet().stream()
//                                                        .filter(property -> property.getName().equals(propertyName))
//                                                        .map(property -> (PropertyInteger) property)
//                                                        .map(blockState::getValue)
//                                                        .findFirst()
//                                                        .ifPresent(value -> {
//                                                            for (int i = 0; i < currentRequiredItems.size(); i++) {
//                                                                ItemStack stack = currentRequiredItems.get(i);
//                                                                currentRequiredItems.set(
//                                                                        i,
//                                                                        new ItemStack(
//                                                                                stack.getItem(),
//                                                                                stack.getCount(),
//                                                                                value
//                                                                        )
//                                                                );
//                                                            }
//                                                        })
//                                        );
//                            }
                            if (block.hasTileEntity(blockState)) {
                                TileEntity tileEntity = schematicBlockContext.world.getTileEntity(schematicBlockContext.pos);
                                if (tileEntity != null) {
                                    tileNbt = tileEntity.serializeNBT();
                                    Arrays.stream(EnumFacing.values())
                                            .filter(side -> tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
                                            .map(side -> tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side))
                                            .filter(Objects::nonNull)
                                            .distinct() // FIXME: this can work wrongly with multi side inventories
                                            .flatMap(itemHandler ->
                                                    IntStream.range(0, itemHandler.getSlots()).mapToObj(itemHandler::getStackInSlot)
                                            )
                                            .filter(stack -> !stack.isEmpty())
                                            .forEach(currentRequiredItems::add);
                                }
                            }
                            currentRequiredItems.removeIf(ItemStack::isEmpty);
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
                                                    .forEach(currentRequiredBlockOffsets::add)
                                    );
                            if (rules.stream().anyMatch(rule -> rule.copyRequiredBlockOffsetsFromProperties)) {
                                for (EnumFacing side : EnumFacing.values()) {
                                    if (blockState.getProperties().keySet().stream()
                                            .filter(property -> property.getName().equals(side.getName()))
                                            .map(property -> (PropertyBool) property)
                                            .anyMatch(blockState::getValue)) {
                                        currentRequiredBlockOffsets.add(new BlockPos(side.getDirectionVec()));
                                    }
                                }
                            }
                            List<IProperty<?>> ignoredProperties = rules.stream()
                                    .map(rule -> rule.ignoredProperties)
                                    .filter(Objects::nonNull)
                                    .flatMap(List::stream)
                                    .flatMap(propertyName ->
                                            blockState.getProperties().keySet().stream()
                                                    .filter(property -> property.getName().equals(propertyName))
                                    )
                                    .collect(Collectors.toList());
                            return new SchematicBlock(
                                    relativePos,
                                    currentRequiredBlockOffsets,
                                    currentRequiredItems,
                                    requiredFluids,
                                    blockState,
                                    ignoredProperties,
                                    tileNbt,
                                    placeBlock,
                                    canBeReplacedWithBlocks
                            );
                        }
                );
            }
        });
    }
}
