package buildcraft.lib.oredictionarytag;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OreDictionaryTags {
    // Gear
    public static INamedTag<Item> GEARS = itemTag("forge:gears");

    public final static INamedTag<Item> GEAR_WOOD = itemTag("forge:gears/wood");
    public final static INamedTag<Item> GEAR_STONE = itemTag("forge:gears/stone");
    public final static INamedTag<Item> GEAR_IRON = itemTag("forge:gears/iron");
    public final static INamedTag<Item> GEAR_GOLD = itemTag("forge:gears/gold");
    public final static INamedTag<Item> GEAR_DIAMOND = itemTag("forge:gears/diamond");

    // Workbench
    public final static INamedTag<Block> WORKBENCHES_BLOCK = blockTag("forge:workbenches");
    public final static INamedTag<Item> WORKBENCHES_ITEM = itemTag("forge:workbenches");

    // Clay
    public final static INamedTag<Item> CLAY = itemTag("forge:clay");

    // Oil
    public final static INamedTag<Fluid> OIL = fluidTag("buildcraft:oil");

    // tools
    public static final INamedTag<Item> WRENCH = itemTag("forge:tools/wrench");
    public static final INamedTag<Item> PAINT_BRUSH = itemTag("buildcraft:paintbrush");

    // Sealant
    public static final INamedTag<Item> SEALANT = itemTag("buildcraft:sealant");

//    // Biome
//    public static final INamedTag<Biome> OIL_GEN = biomeTag("buildcraftenergy:oil_gen");

    // pipe plugs
    public static final INamedTag<Item> WATERPROOF = itemTag("buildcraft:waterproof");

    // soft for robotics
    public static final INamedTag<Block> SOFT = blockTag("buildcraft:soft");

    // Pipe
    public static final INamedTag<Block> PIPE = blockTag("buildcraftcore:pipe");
    // Pipes
    public static final Map<DyeColor, INamedTag<Item>> pipeColorTags = new HashMap<>();

    static {
        Arrays.stream(DyeColor.values()).forEach(c -> pipeColorTags.put(c, itemTag("buildcraft:pipe/" + c.name().toLowerCase(Locale.ROOT))));
        pipeColorTags.put(null, itemTag("buildcraft:pipe/colorless"));
    }

    public static final INamedTag<Item> pipeStructure = itemTag("buildcraft:pipe/structure_cobblestone");

    public static final INamedTag<Item> pipeItemWood = itemTag("buildcraft:pipe/items_wood");
    public static final INamedTag<Item> pipeFluidWood = itemTag("buildcraft:pipe/fluids_wood");
    public static final INamedTag<Item> pipePowerWood = itemTag("buildcraft:pipe/power_wood");

    public static final INamedTag<Item> pipeItemStone = itemTag("buildcraft:pipe/items_stone");
    public static final INamedTag<Item> pipeFluidStone = itemTag("buildcraft:pipe/fluids_stone");
    public static final INamedTag<Item> pipePowerStone = itemTag("buildcraft:pipe/power_stone");

    public static final INamedTag<Item> pipeItemCobble = itemTag("buildcraft:pipe/items_cobblestone");
    public static final INamedTag<Item> pipeFluidCobble = itemTag("buildcraft:pipe/fluids_cobblestone");
    public static final INamedTag<Item> pipePowerCobble = itemTag("buildcraft:pipe/power_cobblestone");

    public static final INamedTag<Item> pipeItemQuartz = itemTag("buildcraft:pipe/items_quartz");
    public static final INamedTag<Item> pipeFluidQuartz = itemTag("buildcraft:pipe/fluids_quartz");
    public static final INamedTag<Item> pipePowerQuartz = itemTag("buildcraft:pipe/power_quartz");

    public static final INamedTag<Item> pipeItemGold = itemTag("buildcraft:pipe/items_gold");
    public static final INamedTag<Item> pipeFluidGold = itemTag("buildcraft:pipe/fluids_gold");
    public static final INamedTag<Item> pipePowerGold = itemTag("buildcraft:pipe/power_gold");

    public static final INamedTag<Item> pipeItemSandstone = itemTag("buildcraft:pipe/items_sandstone");
    public static final INamedTag<Item> pipeFluidSandstone = itemTag("buildcraft:pipe/fluids_sandstone");
    public static final INamedTag<Item> pipePowerSandstone = itemTag("buildcraft:pipe/power_sandstone");

    public static final INamedTag<Item> pipeItemIron = itemTag("buildcraft:pipe/items_iron");
    public static final INamedTag<Item> pipeFluidIron = itemTag("buildcraft:pipe/fluids_iron");
    // public static ItemPipeHolder pipePowerIron= itemTag("buildcraft:pipe/power_iron");

    public static final INamedTag<Item> pipeItemDiamond = itemTag("buildcraft:pipe/items_diamond");
    public static final INamedTag<Item> pipeFluidDiamond = itemTag("buildcraft:pipe/fluids_diamond");
    // public static ItemPipeHolder pipePowerDiamond= itemTag("buildcraft:pipe/power_diamond");

    public static final INamedTag<Item> pipeItemDiaWood = itemTag("buildcraft:pipe/items_diamond_wood");
    public static final INamedTag<Item> pipeFluidDiaWood = itemTag("buildcraft:pipe/fluids_diamond_wood");
//    public static final INamedTag<Item> pipePowerDiaWood= itemTag("buildcraft:pipe/power_diamond_wood");

    public static final INamedTag<Item> pipeItemClay = itemTag("buildcraft:pipe/items_clay");
    public static final INamedTag<Item> pipeFluidClay = itemTag("buildcraft:pipe/fluids_clay");

    public static final INamedTag<Item> pipeItemVoid = itemTag("buildcraft:pipe/items_void");
    public static final INamedTag<Item> pipeFluidVoid = itemTag("buildcraft:pipe/fluids_void");

    public static final INamedTag<Item> pipeItemObsidian = itemTag("buildcraft:pipe/items_obsidian");
    public static final INamedTag<Item> pipeFluidObsidian = itemTag("buildcraft:pipe/fluids_obsidian");

    public static final INamedTag<Item> pipeItemLapis = itemTag("buildcraft:pipe/items_lapis");
    public static final INamedTag<Item> pipeItemDaizuli = itemTag("buildcraft:pipe/items_daizuli");
    public static final INamedTag<Item> pipeItemEmzuli = itemTag("buildcraft:pipe/items_emzuli");
    public static final INamedTag<Item> pipeItemStripes = itemTag("buildcraft:pipe/items_stripes");

    private static INamedTag<Item> itemTag(String name) {
        return ItemTags.createOptional(new ResourceLocation(name));
    }

    private static INamedTag<Fluid> fluidTag(String name) {
        return FluidTags.createOptional(new ResourceLocation(name));
    }

    private static INamedTag<Block> blockTag(String name) {
        return BlockTags.createOptional(new ResourceLocation(name));
    }

//    private static INamedTag<Biome> biomeTag(String name) {
//        return INamedTag.create(Registry.BIOME_REGISTRY, new ResourceLocation(name));
//    }
}
