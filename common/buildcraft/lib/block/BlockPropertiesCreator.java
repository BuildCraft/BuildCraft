package buildcraft.lib.block;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

// Calen
public class BlockPropertiesCreator {
    public static BlockBehaviour.Properties metal() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(5.0F, 10.0F)
                .sound(SoundType.METAL);
    }

    public static BlockBehaviour.Properties decoration() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.NONE)
                .strength(0, 0)
                .noCollission() // 1.18.2: noCollider()
                .instabreak()
                .sound(SoundType.METAL)
                .pushReaction(PushReaction.DESTROY) // 1.18.2: destroyOnPush()
                ;
    }

    public static BlockBehaviour.Properties clay() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.CLAY)
                ;
    }
}
