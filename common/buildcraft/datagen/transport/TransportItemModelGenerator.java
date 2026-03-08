package buildcraft.datagen.transport;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.transport.BCTransport;
import buildcraft.transport.BCTransportBlocks;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.pipe.PipeRegistry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class TransportItemModelGenerator extends BCBaseItemModelGenerator {
    public TransportItemModelGenerator(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, BCTransport.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // pipes
        PipeRegistry.INSTANCE.getAllRegisteredPipes().forEach((def) ->
                {
                    getBuilder(def.identifier.getNamespace() + ":pipe_" + def.identifier.getPath() + "_colorless").parent(BUILTIN_ENTITY);
                    for (DyeColor colour : DyeColor.values()) {
                        getBuilder(def.identifier.getNamespace() + ":pipe_" + def.identifier.getPath() + "_" + colour.getName()).parent(BUILTIN_ENTITY);
                    }
                }
        );

        // filteredBuffer
        withExistingParent(BCTransportBlocks.filteredBuffer.get().getRegistryName().toString(), "buildcrafttransport:block/filtered_buffer");
        // waterproof
        withExistingParent(BCTransportItems.waterproof.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcrafttransport:items/pipewaterproof");
        // plugBlocker
        getBuilder(BCTransportItems.plugBlocker.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);
        // plugPowerAdaptor
        getBuilder(BCTransportItems.plugPowerAdaptor.get().getRegistryName().toString()).parent(BUILTIN_ENTITY);

        // wires
        with16Colours(BCTransportItems.wire.get());
    }

    private void with16Colours(Item item) {
        ResourceLocation reg = item.getRegistryName();

        ItemModelBuilder model = withExistingParent(reg.getNamespace() + ":item/" + reg.getPath(), HANDHELD);
        for (DyeColor colour : DyeColor.values()) {
            model = model
                    .override()
                    .model(
                            withExistingParent(reg.getNamespace() + ":item/" + reg.getPath() + "/" + colour.getName().toLowerCase(), HANDHELD)
                                    .texture("layer0", reg.getNamespace() + ":items/" + reg.getPath() + "/" + colour.getName().toLowerCase())
                    )
                    .predicate(new ResourceLocation("buildcraft", "colour"), colour.getId())
                    .end();
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Transport Item Model Generator";
    }
}
