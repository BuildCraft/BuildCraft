/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.triggers;

import buildcraft.core.triggers.BCAction;
import buildcraft.core.utils.EnumColor;
import buildcraft.core.utils.StringUtils;
import java.util.Locale;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class ActionExtractionPreset extends BCAction {

	private IIcon icon;
	public final EnumColor color;

	public ActionExtractionPreset(EnumColor color) {
		super("buildcraft:extraction.preset." + color.getTag(), "buildcraft.extraction.preset." + color.getTag());

		this.color = color;
	}

	@Override
	public String getDescription() {
		return String.format(StringUtils.localize("gate.action.extraction"), color.getName());
	}

	@Override
	public IIcon getIcon() {
		return icon;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/extraction_preset_" + color.name().toLowerCase(Locale.ENGLISH));
	}
}
