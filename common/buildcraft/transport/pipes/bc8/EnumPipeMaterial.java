package buildcraft.transport.pipes.bc8;

import net.minecraft.block.BlockStone;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public enum EnumPipeMaterial {
    // Extraction
    WOOD(2, new ItemStack(Blocks.planks, 1, OreDictionary.WILDCARD_VALUE)),
    /** Filtered extraction. */
    EMERALD(2, new ItemStack(Items.emerald)),
    // Variations of stone, don't connect to each other
    COBBLESTONE(1, new ItemStack(Blocks.cobblestone)),
    STONE(1, new ItemStack(Blocks.stone)),
    ANDERSITE(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.ANDESITE.getMetadata())),
    ANDERSITE_POLISHED(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.ANDESITE_SMOOTH.getMetadata())),
    DIORITE(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.DIORITE.getMetadata())),
    DIORITE_POLISHED(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.DIORITE_SMOOTH.getMetadata())),
    GRANITE(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.GRANITE.getMetadata())),
    GRANITE_POLISHED(1, new ItemStack(Blocks.stone, 1, BlockStone.EnumType.GRANITE_SMOOTH.getMetadata())),
    // Different functionality
    /** Send all contents only a single direction */
    IRON(2, new ItemStack(Items.iron_ingot)),
    /** Speed up contents */
    GOLD(1, new ItemStack(Items.gold_ingot)),
    /** Only connects to pipes */
    SANDSTONE(1, new ItemStack(Blocks.sandstone, 1, OreDictionary.WILDCARD_VALUE)),
    /** Insertion pipe, will prioritise insertion into an inventory rather than another pipe */
    CLAY(1, new ItemStack(Items.clay_ball)),
    /** Buffer pipe, will pause items if they cannot go directly into an inventory. (Like clay but doesn't put items
     * into a different place.) */
    QUARTZ(1, new ItemStack(Items.quartz)),
    /** Sorts contents based off filters for directions. */
    DIAMOND(7, new ItemStack(Items.diamond)),
    /** Destroys contents */
    VOID(1, new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), new ItemStack(Items.redstone)),
    /** Sucks up items */
    OBSIDIAN(1, new ItemStack(Blocks.obsidian)),
    /** Places items */
    STRIPES(1, new ItemStack(Items.dye, 1, EnumDyeColor.BLACK.getDyeDamage()), new ItemStack(Items.dye, 1, EnumDyeColor.YELLOW.getDyeDamage()));

    public final int maxSprites;
    public final ItemStack ingredient1, ingredient2;

    EnumPipeMaterial(int maxSprites, ItemStack stack) {
        this(maxSprites, stack, stack);
    }

    EnumPipeMaterial(int maxSprites, ItemStack stack1, ItemStack stack2) {
        this.maxSprites = maxSprites;
        ingredient1 = stack1;
        ingredient2 = stack2;
    }

    public static final EnumPipeMaterial[][] STONES = { { COBBLESTONE, STONE }, { ANDERSITE, ANDERSITE_POLISHED }, { DIORITE, DIORITE_POLISHED }, {
        GRANITE, GRANITE_POLISHED } };
}
