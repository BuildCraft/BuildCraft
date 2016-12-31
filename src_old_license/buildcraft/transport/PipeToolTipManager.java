/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.StringUtilBC;
import buildcraft.transport.pipes.PipePowerWood;

@SideOnly(Side.CLIENT)
public final class PipeToolTipManager {

    private static final Map<Class<? extends Pipe<?>>, String> toolTips = new HashMap<>();
    private static final Map<Class<? extends Pipe<?>>, String> shiftToolTips = new HashMap<>();

    static {
        if (!BuildCraftCore.hidePowerNumbers) {
            for (Map.Entry<Class<? extends Pipe<?>>, Integer> pipe : PipeTransportPower.powerCapacities.entrySet()) {
                if (PipePowerWood.class.isAssignableFrom(pipe.getKey())) {
                    continue;
                }

                PipeToolTipManager.addToolTip(pipe.getKey(), String.format("%d RF/t", pipe.getValue()));

                DecimalFormat format = new DecimalFormat();
                format.setMinimumFractionDigits(0);
                format.setMaximumFractionDigits(2);

                switch (PipeTransportPower.lossMode) {
                    case ABSOLUTE:
                        float f1 = PipeTransportPower.powerLosses.get(pipe.getKey());
                        if (f1 != 0) {
                            PipeToolTipManager.addShiftToolTip(pipe.getKey(), String.format("Loss: %s RF/pipe", format.format(f1)));
                        }
                        break;
                    case PERCENTAGE:
                        float f2 = PipeTransportPower.powerResistances.get(pipe.getKey());
                        if (f2 != 0) {
                            PipeToolTipManager.addShiftToolTip(pipe.getKey(), String.format("Loss: %s%%/pipe", format.format(f2)));
                        }
                        break;

                }
            }
        }

        if (!BuildCraftCore.hideFluidNumbers) {
            for (Map.Entry<Class<? extends Pipe<?>>, Integer> pipe : PipeTransportFluids.fluidCapacities.entrySet()) {
                PipeToolTipManager.addToolTip(pipe.getKey(), String.format("%d mB/t", pipe.getValue()));
            }
        }
    }

    /** Deactivate constructor */
    private PipeToolTipManager() {}

    private static void addTipToList(String tipTag, List<String> tips) {
        if (LocaleUtil.canLocalize(tipTag)) {
            String localized = LocaleUtil.localize(tipTag);
            if (localized != null) {
                List<String> lines = StringUtilBC.newLineSplitter.splitToList(localized);
                tips.addAll(lines);
            }
        }
    }

    public static void addToolTip(Class<? extends Pipe<?>> pipe, String toolTip) {
        toolTips.put(pipe, toolTip);
    }

    public static void addShiftToolTip(Class<? extends Pipe<?>> pipe, String toolTip) {
        shiftToolTips.put(pipe, toolTip);
    }

    public static List<String> getToolTip(Class<? extends Pipe<?>> pipe, boolean advanced) {
        List<String> tips = new ArrayList<>();
        addTipToList("tip." + pipe.getSimpleName(), tips);

        String tip = toolTips.get(pipe);
        if (tip != null) {
            tips.add(tip);
        }

        if (GuiScreen.isShiftKeyDown()) {
            tip = shiftToolTips.get(pipe);
            if (tip != null) {
                tips.add(EnumChatFormatting.GRAY + tip);
            }

            addTipToList("tip.shift." + pipe.getSimpleName(), tips);
        }
        return tips;
    }
}
