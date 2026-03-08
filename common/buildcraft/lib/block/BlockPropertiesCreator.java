package buildcraft.lib.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

// Calen
public class BlockPropertiesCreator {
    public static AbstractBlock.Properties createDefaultProperties(Material material) {
        return AbstractBlock.Properties.of(material)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL);
    }
}
