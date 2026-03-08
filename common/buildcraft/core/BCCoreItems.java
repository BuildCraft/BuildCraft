package buildcraft.core;

import buildcraft.api.items.FluidItemDrops;
import buildcraft.core.item.*;
import buildcraft.lib.BCLib;
import buildcraft.lib.item.ItemBC_Neptune;
import buildcraft.lib.item.ItemPropertiesCreator;
import buildcraft.lib.registry.RegistrationHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public final class BCCoreItems {
    private static final RegistrationHelper HELPER = new RegistrationHelper(BCCore.MODID);

    public static RegistryObject<ItemWrench_Neptune> wrench;
    public static RegistryObject<ItemBC_Neptune> gearWood;
    public static RegistryObject<ItemBC_Neptune> gearStone;
    public static RegistryObject<ItemBC_Neptune> gearIron;
    public static RegistryObject<ItemBC_Neptune> gearGold;
    public static RegistryObject<ItemBC_Neptune> gearDiamond;
    // public static RegistryObject<ItemPaintbrush_BC8> paintbrush;
    public static Map<DyeColor, RegistryObject<ItemPaintbrush_BC8>> colourBrushMap = new HashMap<>();
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushClean;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushWhite;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushOrange;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushMagenta;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushLightBlue;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushYellow;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushLime;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushPink;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushSilver;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushCyan;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushPurple;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushBlue;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushBrown;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushGreen;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushRed;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushBlack;
    public static RegistryObject<ItemPaintbrush_BC8> paintbrushGray;
    public static RegistryObject<ItemList_BC8> list;
    public static RegistryObject<ItemMapLocation> mapLocation;
    public static RegistryObject<ItemMarkerConnector> markerConnector;
    public static RegistryObject<ItemVolumeBox> volumeBox;
    public static RegistryObject<ItemFragileFluidContainer> fragileFluidShard;
    public static RegistryObject<ItemGoggles> goggles;

    public static void preInit() {
        wrench = HELPER.addItem("item.wrench", ItemPropertiesCreator.common1().setNoRepair(), ItemWrench_Neptune::new);
        gearWood = HELPER.addItem("item.gear.wood", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
        gearStone = HELPER.addItem("item.gear.stone", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
        gearIron = HELPER.addItem("item.gear.iron", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
        gearGold = HELPER.addItem("item.gear.gold", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
        gearDiamond = HELPER.addItem("item.gear.diamond", ItemPropertiesCreator.common64(), ItemBC_Neptune::new);
//        paintbrush = HELPER.addItem("item.paintbrush", ItemPropertiesCreator.stack1(), ItemPaintbrush_BC8::new);
        paintbrushClean = regPaintBrush(null);
        paintbrushWhite = regPaintBrush(DyeColor.WHITE);
        paintbrushOrange = regPaintBrush(DyeColor.ORANGE);
        paintbrushMagenta = regPaintBrush(DyeColor.MAGENTA);
        paintbrushLightBlue = regPaintBrush(DyeColor.LIGHT_BLUE);
        paintbrushYellow = regPaintBrush(DyeColor.YELLOW);
        paintbrushLime = regPaintBrush(DyeColor.LIME);
        paintbrushPink = regPaintBrush(DyeColor.PINK);
        paintbrushSilver = regPaintBrush(DyeColor.LIGHT_GRAY);
        paintbrushCyan = regPaintBrush(DyeColor.CYAN);
        paintbrushPurple = regPaintBrush(DyeColor.PURPLE);
        paintbrushBlue = regPaintBrush(DyeColor.BLUE);
        paintbrushBrown = regPaintBrush(DyeColor.BROWN);
        paintbrushGreen = regPaintBrush(DyeColor.GREEN);
        paintbrushRed = regPaintBrush(DyeColor.RED);
        paintbrushBlack = regPaintBrush(DyeColor.BLACK);
        paintbrushGray = regPaintBrush(DyeColor.GRAY);
        list = HELPER.addItem("item.list", ItemPropertiesCreator.common1(), ItemList_BC8::new);
        mapLocation = HELPER.addItem("item.map_location", ItemPropertiesCreator.common16(), ItemMapLocation::new);
        markerConnector = HELPER.addItem("item.marker_connector", ItemPropertiesCreator.common64(), ItemMarkerConnector::new);
        volumeBox = HELPER.addItem("item.volume_box", ItemPropertiesCreator.common64(), ItemVolumeBox::new);
        fragileFluidShard = HELPER.addItem("item.fragile_fluid_shard", ItemPropertiesCreator.common1(), ItemFragileFluidContainer::new);
        if (BCLib.DEV) {
            goggles = HELPER.addItem("item.goggles", ItemPropertiesCreator.common1(), ItemGoggles::new);
        }
        FluidItemDrops.item = fragileFluidShard;
    }

    private static RegistryObject<ItemPaintbrush_BC8> regPaintBrush(DyeColor colour) {
        RegistryObject<ItemPaintbrush_BC8> brush = HELPER.addItem("item.paintbrush." + (colour == null ? "clean" : colour.getName()), ItemPropertiesCreator.common1(), (idBC, prop) -> new ItemPaintbrush_BC8(idBC, prop, colour));
        colourBrushMap.put(colour, brush);
        return brush;
    }
}
