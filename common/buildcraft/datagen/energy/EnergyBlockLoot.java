package buildcraft.datagen.energy;

import buildcraft.energy.BCEnergyBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class EnergyBlockLoot extends BlockLootTables {
    @Override
    protected void addTables() {
        dropSelf(BCEnergyBlocks.engineStone.get());
        dropSelf(BCEnergyBlocks.engineIron.get());
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
