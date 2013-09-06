/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
@SideOnly(Side.CLIENT)
public class PipeToolTipManager {

	private static final Map<Class<? extends Pipe>, String> toolTips = new HashMap<Class<? extends Pipe>, String>();

	static {
		for (Map.Entry<Class<? extends Pipe>, Integer> pipe : PipeTransportPower.powerCapacities.entrySet()) {
			PipeToolTipManager.addToolTip(pipe.getKey(), String.format("%d MJ/t", pipe.getValue()));
		}
	}

	public static void addToolTip(Class<? extends Pipe> pipe, String toolTip) {
		toolTips.put(pipe, toolTip);
	}

	public static String getToolTip(Class<? extends Pipe> pipe) {
		String tip = toolTips.get(pipe);
		if (tip != null)
			return tip;
		return StringUtils.localize("tip." + pipe.getSimpleName());
	}
}
