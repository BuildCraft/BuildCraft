package buildcraft.lib.oredictionarytag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class OreDictionaryTags {
    // Gear
    public static TagKey<Item> GEARS = itemTag("forge:gears");

    public final static TagKey<Item> GEAR_WOOD = itemTag("forge:gears/wood");
    public final static TagKey<Item> GEAR_STONE = itemTag("forge:gears/stone");
    public final static TagKey<Item> GEAR_IRON = itemTag("forge:gears/iron");
    public final static TagKey<Item> GEAR_GOLD = itemTag("forge:gears/gold");
    public final static TagKey<Item> GEAR_DIAMOND = itemTag("forge:gears/diamond");

    // Workbench
    public final static TagKey<Block> WORKBENCHES_BLOCK = blockTag("forge:workbenches");
    public final static TagKey<Item> WORKBENCHES_ITEM = itemTag("forge:workbenches");

    // Clay
    public final static TagKey<Item> CLAY = itemTag("forge:clay");

    // Oil
    public final static TagKey<Fluid> OIL = fluidTag("buildcraft:oil");

    // tools
    public static final TagKey<Item> WRENCH = itemTag("forge:tools/wrench");
    public static final TagKey<Item> PAINT_BRUSH = itemTag("buildcraft:paintbrush");

    // Sealant
    public static final TagKey<Item> SEALANT = itemTag("buildcraft:sealant");

    // Biome
    public static final TagKey<Biome> OIL_GEN = biomeTag("buildcraftenergy:oil_gen");

    // pipe plugs
    public static final TagKey<Item> waterproof = itemTag("buildcraft:waterproof");

    // Pipe
    public static final TagKey<Block> PIPE = blockTag("buildcraftcore:pipe");
    // Pipes
    public static final Map<DyeColor, TagKey<Item>> pipeColorTags = new HashMap<>();

    static {
        Arrays.stream(DyeColor.values()).forEach(c -> pipeColorTags.put(c, itemTag("buildcraft:pipe/" + c.name().toLowerCase(Locale.ROOT))));
        pipeColorTags.put(null, itemTag("buildcraft:pipe/colorless"));
    }

    public static final TagKey<Item> pipeStructure = itemTag("buildcraft:pipe/structure_cobblestone");

    public static final TagKey<Item> pipeItemWood = itemTag("buildcraft:pipe/items_wood");
    public static final TagKey<Item> pipeFluidWood = itemTag("buildcraft:pipe/fluids_wood");
    public static final TagKey<Item> pipePowerWood = itemTag("buildcraft:pipe/power_wood");

    public static final TagKey<Item> pipeItemStone = itemTag("buildcraft:pipe/items_stone");
    public static final TagKey<Item> pipeFluidStone = itemTag("buildcraft:pipe/fluids_stone");
    public static final TagKey<Item> pipePowerStone = itemTag("buildcraft:pipe/power_stone");

    public static final TagKey<Item> pipeItemCobble = itemTag("buildcraft:pipe/items_cobblestone");
    public static final TagKey<Item> pipeFluidCobble = itemTag("buildcraft:pipe/fluids_cobblestone");
    public static final TagKey<Item> pipePowerCobble = itemTag("buildcraft:pipe/power_cobblestone");

    public static final TagKey<Item> pipeItemQuartz = itemTag("buildcraft:pipe/items_quartz");
    public static final TagKey<Item> pipeFluidQuartz = itemTag("buildcraft:pipe/fluids_quartz");
    public static final TagKey<Item> pipePowerQuartz = itemTag("buildcraft:pipe/power_quartz");

    public static final TagKey<Item> pipeItemGold = itemTag("buildcraft:pipe/items_gold");
    public static final TagKey<Item> pipeFluidGold = itemTag("buildcraft:pipe/fluids_gold");
    public static final TagKey<Item> pipePowerGold = itemTag("buildcraft:pipe/power_gold");

    public static final TagKey<Item> pipeItemSandstone = itemTag("buildcraft:pipe/items_sandstone");
    public static final TagKey<Item> pipeFluidSandstone = itemTag("buildcraft:pipe/fluids_sandstone");
    public static final TagKey<Item> pipePowerSandstone = itemTag("buildcraft:pipe/power_sandstone");

    public static final TagKey<Item> pipeItemIron = itemTag("buildcraft:pipe/items_iron");
    public static final TagKey<Item> pipeFluidIron = itemTag("buildcraft:pipe/fluids_iron");
    // public static ItemPipeHolder pipePowerIron= itemTag("buildcraft:pipe/power_iron");

    public static final TagKey<Item> pipeItemDiamond = itemTag("buildcraft:pipe/items_diamond");
    public static final TagKey<Item> pipeFluidDiamond = itemTag("buildcraft:pipe/fluids_diamond");
    // public static ItemPipeHolder pipePowerDiamond= itemTag("buildcraft:pipe/power_diamond");

    public static final TagKey<Item> pipeItemDiaWood = itemTag("buildcraft:pipe/items_diamond_wood");
    public static final TagKey<Item> pipeFluidDiaWood = itemTag("buildcraft:pipe/fluids_diamond_wood");
//    public static final TagKey<Item> pipePowerDiaWood= itemTag("buildcraft:pipe/power_diamond_wood");

    public static final TagKey<Item> pipeItemClay = itemTag("buildcraft:pipe/items_clay");
    public static final TagKey<Item> pipeFluidClay = itemTag("buildcraft:pipe/fluids_clay");

    public static final TagKey<Item> pipeItemVoid = itemTag("buildcraft:pipe/items_void");
    public static final TagKey<Item> pipeFluidVoid = itemTag("buildcraft:pipe/fluids_void");

    public static final TagKey<Item> pipeItemObsidian = itemTag("buildcraft:pipe/items_obsidian");
    public static final TagKey<Item> pipeFluidObsidian = itemTag("buildcraft:pipe/fluids_obsidian");

    public static final TagKey<Item> pipeItemLapis = itemTag("buildcraft:pipe/items_lapis");
    public static final TagKey<Item> pipeItemDaizuli = itemTag("buildcraft:pipe/items_daizuli");
    public static final TagKey<Item> pipeItemEmzuli = itemTag("buildcraft:pipe/items_emzuli");
    public static final TagKey<Item> pipeItemStripes = itemTag("buildcraft:pipe/items_stripes");

    private static TagKey<Item> itemTag(String name) {
        return TagKey.create(Registries.ITEM, new ResourceLocation(name));
    }

    private static TagKey<Fluid> fluidTag(String name) {
        return TagKey.create(Registries.FLUID, new ResourceLocation(name));
    }

    private static TagKey<Block> blockTag(String name) {
        return TagKey.create(Registries.BLOCK, new ResourceLocation(name));
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registries.BIOME, new ResourceLocation(name));
    }
}
