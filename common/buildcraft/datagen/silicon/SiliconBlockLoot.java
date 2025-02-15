package buildcraft.datagen.silicon;

import buildcraft.silicon.BCSiliconBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.HashSet;
import java.util.Set;

public class SiliconBlockLoot extends BlockLootSubProvider {
    public SiliconBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
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
