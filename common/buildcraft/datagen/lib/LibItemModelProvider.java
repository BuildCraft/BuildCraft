package buildcraft.datagen.lib;

import buildcraft.datagen.base.BCBaseItemModelGenerator;
import buildcraft.lib.BCLibItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;

public class LibItemModelProvider extends BCBaseItemModelGenerator {
    public LibItemModelProvider(DataGenerator generator, String modid, ExistingFileHelper existingFileHelper) {
        super(generator, modid, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent(BCLibItems.debugger.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:items/debugger");
        withExistingParent(BCLibItems.guide.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:items/guide_book");
        withExistingParent(BCLibItems.guideNote.get().getRegistryName().toString(), GENERATED)
                .texture("layer0", "buildcraftlib:items/guide_note");
    }

    @Nonnull
    @Override
    public String getName() {
        return "BuildCraft Lib Item Model Generator";
    }
}
