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
import java.util.Locale;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class ActionExtractionPreset extends BCAction {

	private Icon icon;
	public final EnumColor color;

	public ActionExtractionPreset(int id, EnumColor color) {
		super(id, "buildcraft.extraction.preset." + color.getTag());

		this.color = color;
	}

	@Override
	public String getDescription() {
		return color.getName() + " Extraction Preset";
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	@Override
	public void registerIcons(IconRegister iconRegister) {
		icon = iconRegister.registerIcon("buildcraft:triggers/extraction_preset_" + color.name().toLowerCase(Locale.ENGLISH));
	}
}
