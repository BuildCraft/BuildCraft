package buildcraft.datagen.core;

import buildcraft.core.BCCoreBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class CoreBlockLoot extends BlockLootTables {
    @Override
    protected void addTables() {
        BCCoreBlocks.decoratedMap.values().forEach(r ->
                dropSelf(r.get())
        );
        dropSelf(BCCoreBlocks.engineWood.get());
        dropSelf(BCCoreBlocks.engineCreative.get());
        dropSelf(BCCoreBlocks.markerVolume.get());
        dropSelf(BCCoreBlocks.markerPath.get());
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
