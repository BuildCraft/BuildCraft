package buildcraft.datagen.factory;

import buildcraft.factory.BCFactoryBlocks;
import buildcraft.factory.BCFactoryItems;
import buildcraft.factory.loot.LootConditionSpreading;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.*;
import net.minecraft.loot.functions.SetCount;

import java.util.HashSet;
import java.util.Set;

public class FactoryBlockLoot extends BlockLootTables {
    @Override
    protected void addTables() {
        dropSelf(BCFactoryBlocks.autoWorkbenchItems.get());
        dropSelf(BCFactoryBlocks.miningWell.get());
        dropSelf(BCFactoryBlocks.pump.get());
        dropSelf(BCFactoryBlocks.floodGate.get());
        dropSelf(BCFactoryBlocks.tank.get());
        dropSelf(BCFactoryBlocks.chute.get());
        dropSelf(BCFactoryBlocks.distiller.get());
        dropSelf(BCFactoryBlocks.heatExchange.get());
        add(BCFactoryBlocks.waterGel.get(),
                LootTable.lootTable()
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantRange.exactly(1))
                                        .add(ItemLootEntry.lootTableItem(BCFactoryItems.gelledWater.get()))
                        )
                        .withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantRange.exactly(1))
                                        .add(ItemLootEntry.lootTableItem(BCFactoryItems.gelledWater.get())
                                                .when(LootConditionSpreading.builder())
                                                .apply(SetCount.setCount(RandomValueRange.between(0, 1)))
                                        )
                        )
        );
    }

    // Calen: without these: IllegalStateException: Missing loottable 'minecraft:blocks/stone' for 'minecraft:stone'
    private final Set<Block> knownBlocks = new HashSet<>();

    @Override
    protected void add(Block block, LootTable.Builder builder) {
        super.add(block, builder);
        knownBlocks.add(block);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return knownBlocks;
    }
}
