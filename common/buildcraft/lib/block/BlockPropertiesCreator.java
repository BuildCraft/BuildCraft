package buildcraft.lib.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;

// Calen
public class BlockPropertiesCreator {
    public static BlockBehaviour.Properties createDefaultProperties(Material material) {
        return BlockBehaviour.Properties.of(material)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL);
    }
}
