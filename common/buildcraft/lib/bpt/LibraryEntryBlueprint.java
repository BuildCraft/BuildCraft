package buildcraft.lib.bpt;

import java.io.IOException;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.data.NBTSquishConstants;

import buildcraft.lib.library.ILibraryEntryData;
import buildcraft.lib.misc.data.ZipFileHelper;

public class LibraryEntryBlueprint implements ILibraryEntryData {
    private static final String LOC_BPT_NAME = "blueprint.nbt.comp";

    public static final String KIND = "nbpt";

    private final Blueprint blueprint;

    public LibraryEntryBlueprint(ZipFileHelper helper) throws IOException {
        NBTTagCompound nbt = helper.getNbtEntry(LOC_BPT_NAME);
        blueprint = new Blueprint(nbt);
    }

    public LibraryEntryBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    @Override
    public void write(ZipFileHelper file) {
        file.addNbtEntry(LOC_BPT_NAME, "", blueprint.serializeNBT(), NBTSquishConstants.BUILDCRAFT_V1_COMPRESSED);
    }
}
