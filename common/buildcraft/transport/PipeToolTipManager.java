/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.utils.StringUtils;

@SideOnly(Side.CLIENT)
public final class PipeToolTipManager {

	private static final Map<Class<? extends Pipe<?>>, String> toolTips = new HashMap<Class<? extends Pipe<?>>, String>();

	static {
		if (!BuildCraftCore.hidePowerNumbers && !BuildCraftTransport.usePipeLoss) {
			for (Map.Entry<Class<? extends Pipe<?>>, Integer> pipe : PipeTransportPower.powerCapacities.entrySet()) {
				PipeToolTipManager.addToolTip(pipe.getKey(), String.format("%d RF/t", pipe.getValue()));
			}
		}

		if (!BuildCraftCore.hideFluidNumbers) {
			for (Map.Entry<Class<? extends Pipe<?>>, Integer> pipe : PipeTransportFluids.fluidCapacities.entrySet()) {
				PipeToolTipManager.addToolTip(pipe.getKey(), String.format("%d mB/t", pipe.getValue()));
			}
		}
	}

	/**
	 * Deactivate constructor
	 */
	private PipeToolTipManager() {
	}

	private static void addTipToList(String tipTag, List<String> tips) {
		if (StringUtils.canLocalize(tipTag)) {
			String localized = StringUtils.localize(tipTag);
			if (localized != null) {
				List<String> lines = StringUtils.newLineSplitter.splitToList(localized);
				tips.addAll(lines);
			}
		}
	}

	public static void addToolTip(Class<? extends Pipe<?>> pipe, String toolTip) {
		toolTips.put(pipe, toolTip);
	}

	public static List<String> getToolTip(Class<? extends Pipe<?>> pipe, boolean advanced) {
		List<String> tips = new ArrayList<String>();
		addTipToList("tip." + pipe.getSimpleName(), tips);

		String tip = toolTips.get(pipe);
		if (tip != null) {
			tips.add(tip);
		}

		if (GuiScreen.isShiftKeyDown()) {
			addTipToList("tip.shift." + pipe.getSimpleName(), tips);
		}
		return tips;
	}
}
