package buildcraft.transport.gate;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum EnumGateMaterial {
    CLAY_BRICK(Blocks.BRICK_BLOCK, 1, false),
    IRON(Blocks.IRON_BLOCK, 2, true),
    NETHER_BRICK(Blocks.NETHER_BRICK, 4, true),
    GOLD(Blocks.GOLD_BLOCK, 8, true);

    public static final EnumGateMaterial[] VALUES = values();

    public final Block block;
    public final int numSlots;
    public final boolean canBeModified;
    public final String tag = name().toLowerCase(Locale.ROOT);

    private EnumGateMaterial(Block block, int numSlots, boolean canBeModified) {
        this.block = block;
        this.numSlots = numSlots;
        this.canBeModified = canBeModified;
    }

    public static EnumGateMaterial getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateMaterial.CLAY_BRICK;
        }
        return VALUES[ord];
    }
}
