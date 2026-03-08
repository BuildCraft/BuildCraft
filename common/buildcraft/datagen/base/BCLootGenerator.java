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
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTable.Builder;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BCLootGenerator extends LootTableProvider {
    public BCLootGenerator(DataGenerator generator) {
        super(generator);
    }

    private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> tables = ImmutableList.of(
            Pair.of(CoreBlockLoot::new, LootParameterSets.BLOCK),
            Pair.of(BuildersBlockLoot::new, LootParameterSets.BLOCK),
            Pair.of(EnergyBlockLoot::new, LootParameterSets.BLOCK),
            Pair.of(FactoryBlockLoot::new, LootParameterSets.BLOCK),
            Pair.of(SiliconBlockLoot::new, LootParameterSets.BLOCK),
            Pair.of(TransportBlockLoot::new, LootParameterSets.BLOCK)
    );

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootParameterSet>> getTables() {
        return tables;
    }

    @Override
    public String getName() {
        return "BuildCraft Loot Tables Generator";
    }
}
