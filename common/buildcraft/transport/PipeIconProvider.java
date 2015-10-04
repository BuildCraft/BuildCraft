/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.EnumColor;
import buildcraft.BuildCraftCore;

public class PipeIconProvider implements IIconProvider {

    public enum TYPE {

        PipeStructureCobblestone("pipeStructureCobblestone"),
        //
        PipeItemsCobbleStone("pipeItemsCobblestone"),
        //
        PipeItemsDiamond_Item("pipeItemsDiamond_item"),
        PipeItemsDiamond_Center("pipeItemsDiamond_center"),
        PipeItemsDiamond_Down("pipeItemsDiamond_down"),
        PipeItemsDiamond_Up("pipeItemsDiamond_up"),
        PipeItemsDiamond_North("pipeItemsDiamond_north"),
        PipeItemsDiamond_South("pipeItemsDiamond_south"),
        PipeItemsDiamond_West("pipeItemsDiamond_west", "pipeItemsDiamond_west_cb"),
        PipeItemsDiamond_East("pipeItemsDiamond_east"),
        //
        PipeItemsLapis_Black("pipeItemsLapis_black"),
        PipeItemsLapis_Red("pipeItemsLapis_red"),
        PipeItemsLapis_Green("pipeItemsLapis_green"),
        PipeItemsLapis_Brown("pipeItemsLapis_brown"),
        PipeItemsLapis_Blue("pipeItemsLapis_blue"),
        PipeItemsLapis_Purple("pipeItemsLapis_purple"),
        PipeItemsLapis_Cyan("pipeItemsLapis_cyan"),
        PipeItemsLapis_LightGray("pipeItemsLapis_lightgray"),
        PipeItemsLapis_Gray("pipeItemsLapis_gray"),
        PipeItemsLapis_Pink("pipeItemsLapis_pink"),
        PipeItemsLapis_Lime("pipeItemsLapis_lime"),
        PipeItemsLapis_Yellow("pipeItemsLapis_yellow"),
        PipeItemsLapis_LightBlue("pipeItemsLapis_lightblue"),
        PipeItemsLapis_Magenta("pipeItemsLapis_magenta"),
        PipeItemsLapis_Orange("pipeItemsLapis_orange"),
        PipeItemsLapis_White("pipeItemsLapis_white"),
        //
        PipeItemsDaizuli_Black("pipeItemsDaizuli_black"),
        PipeItemsDaizuli_Red("pipeItemsDaizuli_red"),
        PipeItemsDaizuli_Green("pipeItemsDaizuli_green"),
        PipeItemsDaizuli_Brown("pipeItemsDaizuli_brown"),
        PipeItemsDaizuli_Blue("pipeItemsDaizuli_blue"),
        PipeItemsDaizuli_Purple("pipeItemsDaizuli_purple"),
        PipeItemsDaizuli_Cyan("pipeItemsDaizuli_cyan"),
        PipeItemsDaizuli_LightGray("pipeItemsDaizuli_lightgray"),
        PipeItemsDaizuli_Gray("pipeItemsDaizuli_gray"),
        PipeItemsDaizuli_Pink("pipeItemsDaizuli_pink"),
        PipeItemsDaizuli_Lime("pipeItemsDaizuli_lime"),
        PipeItemsDaizuli_Yellow("pipeItemsDaizuli_yellow"),
        PipeItemsDaizuli_LightBlue("pipeItemsDaizuli_lightblue"),
        PipeItemsDaizuli_Magenta("pipeItemsDaizuli_magenta"),
        PipeItemsDaizuli_Orange("pipeItemsDaizuli_orange"),
        PipeItemsDaizuli_White("pipeItemsDaizuli_white"),
        PipeAllDaizuli_Solid("pipeAllDaizuli_solid"),
        //
        PipeItemsWood_Standard("pipeItemsWood_standard"),
        PipeAllWood_Solid("pipeAllWood_solid"),
        //
        PipeItemsEmerald_Standard("pipeItemsEmerald_standard"),
        PipeAllEmerald_Solid("pipeAllEmerald_solid"),
        //
        PipeItemsEmzuli_Standard("pipeItemsEmzuli_standard"),
        PipeAllEmzuli_Solid("pipeAllEmzuli_solid"),
        //
        PipeItemsGold("pipeItemsGold"),
        //
        PipeItemsIron_Standard("pipeItemsIron_standard"),
        PipeAllIron_Solid("pipeAllIron_solid"),
        //
        PipeItemsObsidian("pipeItemsObsidian"),
        PipeItemsSandstone("pipeItemsSandstone"),
        PipeItemsStone("pipeItemsStone"),
        PipeItemsQuartz("pipeItemsQuartz"),
        PipeItemsClay("pipeItemsClay"),
        PipeItemsVoid("pipeItemsVoid"),
        //
        PipeFluidsCobblestone("pipeFluidsCobblestone"),
        PipeFluidsWood_Standard("pipeFluidsWood_standard"),
        PipeFluidsEmerald_Standard("pipeFluidsEmerald_standard"),
        PipeFluidsQuartz("pipeFluidsQuartz"),
        PipeFluidsGold("pipeFluidsGold"),
        PipeFluidsIron_Standard("pipeFluidsIron_standard"),
        PipeFluidsSandstone("pipeFluidsSandstone"),
        PipeFluidsStone("pipeFluidsStone"),
        PipeFluidsVoid("pipeFluidsVoid"),
        //
        PipeFluidsDiamond_Item("pipeFluidsDiamond_item"),
        PipeFluidsDiamond_Center("pipeFluidsDiamond_center"),
        PipeFluidsDiamond_Down("pipeFluidsDiamond_down"),
        PipeFluidsDiamond_Up("pipeFluidsDiamond_up"),
        PipeFluidsDiamond_North("pipeFluidsDiamond_north"),
        PipeFluidsDiamond_South("pipeFluidsDiamond_south"),
        PipeFluidsDiamond_West("pipeFluidsDiamond_west", "pipeFluidsDiamond_west_cb"),
        PipeFluidsDiamond_East("pipeFluidsDiamond_east"),
        //
        PipePowerDiamond("pipePowerDiamond"),
        PipePowerGold("pipePowerGold"),
        PipePowerQuartz("pipePowerQuartz"),
        PipePowerStone("pipePowerStone"),
        PipePowerSandstone("pipePowerSandstone"),
        PipePowerCobblestone("pipePowerCobblestone"),
        PipePowerWood_Standard("pipePowerWood_standard"),
        PipePowerEmerald_Standard("pipePowerEmerald_standard"),
        //
        PipePowerIronM2("pipePowerIronM2"),
        PipePowerIronM4("pipePowerIronM4"),
        PipePowerIronM8("pipePowerIronM8"),
        PipePowerIronM16("pipePowerIronM16"),
        PipePowerIronM32("pipePowerIronM32"),
        PipePowerIronM64("pipePowerIronM64"),
        PipePowerIronM128("pipePowerIronM128"),
        //
        PipeRobotStation("pipeRobotStation"),
        PipeRobotStationReserved("pipeRobotStationReserved"),
        PipeRobotStationLinked("pipeRobotStationLinked"),
        //
        Power_Normal("core:blocks/misc/texture_cyan"),
        Power_Overload("core:blocks/misc/texture_red_lit"),
        Stripes("pipeStripes"),
        //
        PipeStainedOverlay("pipeStainedOverlay"),
        PipeLens("pipeLens"),
        PipeFilter("pipeFilter"),
        PipeLensOverlay("pipeLensOverlay"),
        PipePlug("pipePlug"),
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
        private TextureAtlasSprite icon;

        private TYPE(String iconTag, String iconTagColorBlind) {
            this.iconTag = iconTag;
            this.iconTagColorBlind = iconTagColorBlind;
        }

        private TYPE(String iconTag) {
            this(iconTag, iconTag);
        }

        private void registerIcon(TextureMap iconRegister) {
            String name = BuildCraftCore.colorBlindMode ? iconTagColorBlind : iconTag;
            if (name.indexOf(":") < 0) {
                name = "transport:pipes/" + name;
            }
            icon = iconRegister.registerSprite(new ResourceLocation("buildcraft" + name));
        }

        public TextureAtlasSprite getIcon() {
            return icon;
        }
    }

    public static final Map<EnumFacing, TYPE> diamondPipeItems, diamondPipeFluids;
    public static final Map<EnumColor, TYPE> lapisPipe, dazuliPipe;

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

        for (EnumColor face : EnumColor.VALUES) {
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
