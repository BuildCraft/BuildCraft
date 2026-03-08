package buildcraft.datagen.silicon;

import buildcraft.silicon.BCSiliconBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class SiliconBlockLoot extends BlockLootTables {
    @Override
    protected void addTables() {
        dropSelf(BCSiliconBlocks.laser.get());
        dropSelf(BCSiliconBlocks.assemblyTable.get());
        dropSelf(BCSiliconBlocks.advancedCraftingTable.get());
        dropSelf(BCSiliconBlocks.integrationTable.get());
        dropSelf(BCSiliconBlocks.chargingTable.get());
        dropSelf(BCSiliconBlocks.programmingTable.get());
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
