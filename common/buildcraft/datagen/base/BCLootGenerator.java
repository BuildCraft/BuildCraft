package buildcraft.datagen.base;

import buildcraft.datagen.builders.BuildersBlockLoot;
import buildcraft.datagen.core.CoreBlockLoot;
import buildcraft.datagen.energy.EnergyBlockLoot;
import buildcraft.datagen.factory.FactoryBlockLoot;
import buildcraft.datagen.silicon.SiliconBlockLoot;
import buildcraft.datagen.transport.TransportBlockLoot;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BCLootGenerator extends LootTableProvider {
    public BCLootGenerator(DataGenerator generator) {
        super(generator);
    }

    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> tables = ImmutableList.of(
            Pair.of(CoreBlockLoot::new, LootContextParamSets.BLOCK),
            Pair.of(BuildersBlockLoot::new, LootContextParamSets.BLOCK),
            Pair.of(EnergyBlockLoot::new, LootContextParamSets.BLOCK),
            Pair.of(FactoryBlockLoot::new, LootContextParamSets.BLOCK),
            Pair.of(SiliconBlockLoot::new, LootContextParamSets.BLOCK),
            Pair.of(TransportBlockLoot::new, LootContextParamSets.BLOCK)
    );

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return tables;
    }

    @Override
    public String getName() {
        return "BuildCraft Loot Tables Generator";
    }
}
