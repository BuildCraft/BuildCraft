package buildcraft.datagen.builders;

import buildcraft.builders.BCBuildersBlocks;
import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class BuildersBlockLoot extends BlockLootTables {
    @Override
    protected void addTables() {
        dropSelf(BCBuildersBlocks.filler.get());
        dropSelf(BCBuildersBlocks.builder.get());
        dropSelf(BCBuildersBlocks.architect.get());
        dropSelf(BCBuildersBlocks.library.get());
        dropSelf(BCBuildersBlocks.replacer.get());
        dropSelf(BCBuildersBlocks.quarry.get());
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
