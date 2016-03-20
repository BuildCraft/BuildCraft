/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Map;
import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IIconProvider;

public class PipeIconProvider implements IIconProvider {
    public enum TYPE {
        PipeStructureCobblestone("cobblestone_structure"),
        //
        PipeItemsCobbleStone("cobblestone_item"),
        //
        PipeItemsDiamond_Item("diamond_item_itemstack"),
        PipeItemsDiamond_Center("diamond_item"),
        PipeItemsDiamond_Down("diamond_item_down"),
        PipeItemsDiamond_Up("diamond_item_up"),
        PipeItemsDiamond_North("diamond_item_north"),
        PipeItemsDiamond_South("diamond_item_south"),
        PipeItemsDiamond_West("diamond_item_west", "diamond_item_west_cb"),
        PipeItemsDiamond_East("diamond_item_east"),
        //
        PipeItemsLapis_Black("lapis_item_black"),
        PipeItemsLapis_Red("lapis_item_red"),
        PipeItemsLapis_Green("lapis_item_green"),
        PipeItemsLapis_Brown("lapis_item_brown"),
        PipeItemsLapis_Blue("lapis_item_blue"),
        PipeItemsLapis_Purple("lapis_item_purple"),
        PipeItemsLapis_Cyan("lapis_item_cyan"),
        PipeItemsLapis_LightGray("lapis_item_lightgray"),
        PipeItemsLapis_Gray("lapis_item_gray"),
        PipeItemsLapis_Pink("lapis_item_pink"),
        PipeItemsLapis_Lime("lapis_item_lime"),
        PipeItemsLapis_Yellow("lapis_item_yellow"),
        PipeItemsLapis_LightBlue("lapis_item_lightblue"),
        PipeItemsLapis_Magenta("lapis_item_magenta"),
        PipeItemsLapis_Orange("lapis_item_orange"),
        PipeItemsLapis_White("lapis_item_white"),
        //
        PipeItemsDaizuli_Black("dazuli_item_black"),
        PipeItemsDaizuli_Red("dazuli_item_red"),
        PipeItemsDaizuli_Green("dazuli_item_green"),
        PipeItemsDaizuli_Brown("dazuli_item_brown"),
        PipeItemsDaizuli_Blue("dazuli_item_blue"),
        PipeItemsDaizuli_Purple("dazuli_item_purple"),
        PipeItemsDaizuli_Cyan("dazuli_item_cyan"),
        PipeItemsDaizuli_LightGray("dazuli_item_lightgray"),
        PipeItemsDaizuli_Gray("dazuli_item_gray"),
        PipeItemsDaizuli_Pink("dazuli_item_pink"),
        PipeItemsDaizuli_Lime("dazuli_item_lime"),
        PipeItemsDaizuli_Yellow("dazuli_item_yellow"),
        PipeItemsDaizuli_LightBlue("dazuli_item_lightblue"),
        PipeItemsDaizuli_Magenta("dazuli_item_magenta"),
        PipeItemsDaizuli_Orange("dazuli_item_orange"),
        PipeItemsDaizuli_White("dazuli_item_white"),
        PipeItemsDaizuli_Solid("dazuli_item_filled"),
        //
        PipeItemsWood_Standard("wood_item_clear"),
        PipeItemsWood_Solid("wood_item_filled"),
        //
        PipeItemsEmerald_Standard("emerald_item_clear"),
        PipeItemsEmerald_Solid("emerald_item_filled"),
        //
        PipeItemsEmzuli_Standard("emzuli_item_clear"),
        PipeAllEmzuli_Solid("emzuli_item_filled"),
        //
        PipeItemsGold("gold_item"),
        //
        PipeItemsIron_Standard("iron_item_clear"),
        PipeItemsIron_Solid("iron_item_filled"),
        //
        PipeItemsObsidian("obsidian_item"),
        PipeItemsSandstone("sandstone_item"),
        PipeItemsStone("stone_item"),
        PipeItemsQuartz("quartz_item"),
        PipeItemsClay("clay_item"),
        PipeItemsVoid("void_item"),
        //
        PipeFluidsCobblestone("cobblestone_fluid"),
        //
        PipeFluidsWood_Standard("wood_fluid_clear"),
        PipeFluidsWood_Solid("wood_fluid_filled"),
        //
        PipeFluidsEmerald_Standard("emerald_fluid_clear"),
        PipeFluidsEmerald_Solid("emerald_fluid_filled"),
        PipeFluidsQuartz("quartz_fluid"),
        PipeFluidsGold("gold_fluid"),
        PipeFluidsIron_Standard("iron_fluid_clear"),
        PipeFluidsIron_Solid("iron_fluid_filled"),
        PipeFluidsSandstone("sandstone_fluid"),
        PipeFluidsStone("stone_fluid"),
        PipeFluidsVoid("void_fluid"),
        PipeFluidsClay("clay_fluid"),
        //
        PipeFluidsDiamond_Item("diamond_fluid_itemstack"),
        PipeFluidsDiamond_Center("diamond_fluid"),
        PipeFluidsDiamond_Down("diamond_fluid_down"),
        PipeFluidsDiamond_Up("diamond_fluid_up"),
        PipeFluidsDiamond_North("diamond_fluid_north"),
        PipeFluidsDiamond_South("diamond_fluid_south"),
        PipeFluidsDiamond_West("diamond_fluid_west", "diamond_fluid_west_cb"),
        PipeFluidsDiamond_East("diamond_fluid_east"),
        //
        PipePowerDiamond("diamond_power"),
        PipePowerGold("gold_power"),
        PipePowerQuartz("quartz_power"),
        PipePowerStone("stone_power"),
        PipePowerSandstone("sandstone_power"),
        PipePowerCobblestone("cobblestone_power"),
        //
        PipePowerWood_Standard("wood_power_clear"),
        PipePowerWood_Solid("wood_power_filled"),
        //
        PipePowerEmerald_Standard("emerald_power_clear"),
        PipePowerEmerald_Solid("emerald_power_filled"),
        //
        PipePowerIronM2("iron_power_m2"),
        PipePowerIronM4("iron_power_m4"),
        PipePowerIronM8("iron_power_m8"),
        PipePowerIronM16("iron_power_m16"),
        PipePowerIronM32("iron_power_m32"),
        PipePowerIronM64("iron_power_m64"),
        PipePowerIronM128("iron_power_m128"),
        //
        PipeRobotStation("robot_station"),
        PipeRobotStationReserved("robot_station_reserved"),
        PipeRobotStationLinked("robot_station_linked"),
        //
        Power_Normal("core:blocks/misc/texture_cyan"),
        Power_Overload("core:blocks/misc/texture_red_lit"),
        PipeItemsStripes("stripes_item"),
        //
        PipeStainedOverlay("overlay_stained"),
        PipeLens("lens"),
        PipeFilter("filter"),
        PipeLensOverlay("overlay_lens"),
        PipePlug("plug"),
        //
        TransparentFacade("transparent_facade"),
        Transparent("core:misc/transparent"),
        //
        /* PipePowerAdapterTop("pipePowerAdapterTop"), PipePowerAdapterSide("pipePowerAdapterSide"),
         * PipePowerAdapterBottom("pipePowerAdapterBottom"), */
        //
        ItemBox("itemBox");
        public static final TYPE[] VALUES = values();
        private final String iconTag;
        private final String iconTagColorBlind;

        @SideOnly(Side.CLIENT)
        private TextureAtlasSprite icon;

        TYPE(String iconTag, String iconTagColorBlind) {
            this.iconTag = iconTag;
            this.iconTagColorBlind = iconTagColorBlind;
        }

        TYPE(String iconTag) {
            this(iconTag, iconTag);
        }

        @SideOnly(Side.CLIENT)
        private void registerIcon(TextureMap iconRegister) {
            String name = BuildCraftCore.colorBlindMode ? iconTagColorBlind : iconTag;
            if (!name.contains(":")) {
                name = "transport:pipes/" + name;
            }
            icon = iconRegister.registerSprite(new ResourceLocation("buildcraft" + name));
        }

        public TextureAtlasSprite getIcon() {
            return icon;
        }
    }

    public static final Map<EnumFacing, TYPE> diamondPipeItems, diamondPipeFluids;
    public static final Map<EnumDyeColor, TYPE> lapisPipe, dazuliPipe;

    static {
        diamondPipeItems = Maps.newHashMap();
        diamondPipeFluids = Maps.newHashMap();
        for (EnumFacing face : EnumFacing.VALUES) {
            diamondPipeItems.put(face, TYPE.VALUES[TYPE.PipeItemsDiamond_Down.ordinal() + face.ordinal()]);
            diamondPipeFluids.put(face, TYPE.VALUES[TYPE.PipeFluidsDiamond_Down.ordinal() + face.ordinal()]);
        }
        diamondPipeItems.put(null, TYPE.PipeItemsDiamond_Center);
        diamondPipeFluids.put(null, TYPE.PipeFluidsDiamond_Center);

        lapisPipe = Maps.newHashMap();
        dazuliPipe = Maps.newHashMap();

        for (EnumDyeColor face : EnumDyeColor.values()) {
            lapisPipe.put(face, TYPE.VALUES[TYPE.PipeItemsLapis_Black.ordinal() + face.ordinal()]);
            dazuliPipe.put(face, TYPE.VALUES[TYPE.PipeItemsDaizuli_Black.ordinal() + face.ordinal()]);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIcon(int pipeIconIndex) {
        if (pipeIconIndex == -1) {
            return null;
        }
        return TYPE.VALUES[pipeIconIndex].icon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(TextureMap iconRegister) {
        for (TYPE type : TYPE.VALUES) {
            type.registerIcon(iconRegister);
        }
    }
}
