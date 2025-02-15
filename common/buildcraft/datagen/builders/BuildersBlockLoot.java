package buildcraft.datagen.builders;

import buildcraft.builders.BCBuildersBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class BuildersBlockLoot extends BlockLootSubProvider {
    public BuildersBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
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
