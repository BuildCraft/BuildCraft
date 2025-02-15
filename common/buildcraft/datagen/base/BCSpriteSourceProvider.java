package buildcraft.datagen.base;

import buildcraft.api.BCModules;
import buildcraft.lib.BCLibEventDistModBus;
import buildcraft.transport.BCTransportSprites;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import java.util.Optional;

public class BCSpriteSourceProvider extends SpriteSourceProvider {
    public BCSpriteSourceProvider(PackOutput output, ExistingFileHelper fileHelper) {
        super(output, fileHelper, BCModules.BUILDCRAFT);
    }

    @Override
    protected void addSources() {
        BCTransportSprites.onDatagenTextureRegister(this::addToBlockAtlas);
        BCLibEventDistModBus.onDatagenTextureRegister(this::addToBlockAtlas, existingFileHelper);
    }

    protected void addToBlockAtlas(ResourceLocation spriteLocation) {
        SourceList atlas = atlas(BLOCKS_ATLAS);
        atlas.addSource(new SingleFile(spriteLocation, Optional.empty()));
    }
}
