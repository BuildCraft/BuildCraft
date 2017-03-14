package buildcraft.builders.snapshot;

import buildcraft.lib.cap.CapabilityHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
                } else {
                    Item itemFromBlock = Item.getItemFromBlock(block);
                    if (itemFromBlock != Items.AIR) {
                        requiredItems.add(new ItemStack(itemFromBlock));
                    }
                }
                // Add schematic generator
                schematicFactories.put(block, schematicBlockContext -> {
                    BlockPos relativePos = schematicBlockContext.pos.subtract(schematicBlockContext.basePos);
                    List<ItemStack> currentRequiredItems = new ArrayList<>(requiredItems);
                    IBlockState blockState = schematicBlockContext.world.getBlockState(schematicBlockContext.pos);
                    NBTTagCompound tileNbt = null;
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
                    return new SchematicBlock(relativePos, currentRequiredItems, blockState, tileNbt);
                });
            }
        });
    }
}
