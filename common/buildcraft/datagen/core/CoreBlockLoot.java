package buildcraft.datagen.core;

import buildcraft.core.BCCoreBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class CoreBlockLoot extends BlockLootSubProvider {
    public CoreBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
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
