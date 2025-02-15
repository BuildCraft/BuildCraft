package buildcraft.datagen.lib;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.lib.BCLibItems;
import buildcraft.transport.BCTransport;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class LibItemModelProvider extends BCBaseItemModelGenerator {
    public LibItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BCTransport.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(BCLibItems.debugger.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:item/debugger");
        withExistingParent(BCLibItems.guide.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:item/guide_book");
        withExistingParent(BCLibItems.guideNote.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:item/guide_note");
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Lib Item Model Generator";
    }
}
