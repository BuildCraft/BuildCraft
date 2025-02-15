package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Set;

public class BCBaseLootTableProvider extends LootTableProvider {
    private final BCModules module;

    public BCBaseLootTableProvider(BCModules module, PackOutput p_254123_, Set<ResourceLocation> p_254481_, List<SubProviderEntry> p_253798_) {
        super(p_254123_, p_254481_, p_253798_);
        this.module = module;
    }

    public String getName() {
        return module.camelCaseName + "Loot Tables";
    }
}
