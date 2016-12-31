package buildcraft.builders.schematic;

import buildcraft.api.schematic.SchematicBlock;
import buildcraft.api.schematic.SchematicBlockContext;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public enum SchematicsLoader {
    INSTANCE;

    public final Map<Block, Function<SchematicBlockContext, SchematicBlock>> schematicFactories = new HashMap<>();

    public void loadAll() {
        schematicFactories.clear();
        Block.REGISTRY.forEach(block -> {
            if (block == null || block.getRegistryName() == null) {
                return;
            }
            String domain = block.getRegistryName().getResourceDomain();
            if (RulesLoader.INSTANCE.readDomains.contains(domain)) {
                Set<JsonRule> rules = RulesLoader.INSTANCE.rules.stream()
                        .filter(rule -> rule.blocks != null)
                        .filter(rule ->
                                rule.blocks.stream()
                                        .map(Block::getBlockFromName)
                                        .anyMatch(Predicate.isEqual(block))
                        )
                        .collect(Collectors.toCollection(HashSet::new));
                int oldSize = rules.size();
                while(true) {
                    new ArrayList<>(rules).stream()
                            .filter(rule -> rule.parentNames != null)
                            .flatMap(rule -> rule.parentNames.stream())
                            .flatMap(ruleName ->
                                    RulesLoader.INSTANCE.rules.stream()
                                            .filter(rule -> Objects.equals(rule.name, ruleName))
                            )
                            .forEach(rules::add);
                    if(oldSize == rules.size()) {
                        break;
                    } else {
                        oldSize = rules.size();
                    }
                }
                List<ItemStack> requiredItems = new ArrayList<>();
                if (rules.stream().filter(rule -> rule.requiredItems != null).count() > 0) {
                    rules.stream()
                            .filter(rule -> rule.requiredItems != null)
                            .map(rule -> rule.requiredItems)
                            .flatMap(names ->
                                    names.stream()
                                            .map(itemName -> itemName.contains("@") ? itemName : itemName + "@0")
                                            .map(itemName ->
                                                    new ItemStack(
                                                            Item.getByNameOrId(itemName.substring(0, itemName.indexOf("@"))),
                                                            1,
                                                            Integer.parseInt(itemName.substring(itemName.indexOf("@") + 1))
                                                    )
                                            )
                            )
                            .filter(Objects::nonNull)
                            .forEach(requiredItems::add);
                } else {
                    Item itemFromBlock = Item.getItemFromBlock(block);
                    if (itemFromBlock != null) {
                        requiredItems.add(new ItemStack(itemFromBlock));
                    }
                }
                schematicFactories.put(block, schematicBlockContext -> {
                    BlockPos relativePos = schematicBlockContext.pos.subtract(schematicBlockContext.basePos);
                    IBlockState blockState = schematicBlockContext.world.getBlockState(schematicBlockContext.pos);
                    return new SchematicBlock(relativePos, blockState, requiredItems);
                });
            }
        });
    }
}
