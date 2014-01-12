/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtils;
import java.util.Locale;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public final class GateDefinition {

	private GateDefinition() {
	}

	public static String getLocalizedName(GateMaterial material, GateLogic logic) {
		if (material == GateMaterial.REDSTONE)
			return StringUtils.localize("gate.name.basic");
		return String.format(StringUtils.localize("gate.name"), StringUtils.localize("gate.material." + material.getTag()), StringUtils.localize("gate.logic." + logic.getTag()));
	}

	public static enum GateMaterial {

		REDSTONE("gate_interface_1.png", 146, 1, false), IRON("gate_interface_2.png", 164, 2, false), GOLD("gate_interface_3.png", 200, 4, true), DIAMOND("gate_interface_4.png", 200, 8, true);
		public static final GateMaterial[] VALUES = values();
		public final ResourceLocation guiFile;
		public final int guiHeight;
		public final int numSlots;
		public final boolean hasParameterSlot;
		private IIcon iconBlock;
		private IIcon iconItem;

		public IIcon getIconBlock() {
			return iconBlock;
		}

		public IIcon getIconItem() {
			return iconItem;
		}

		public String getTag() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		public void registerBlockIcon(IIconRegister iconRegister) {
			if (this != REDSTONE)
				iconBlock = iconRegister.registerIcon("buildcraft:gates/gate_material_" + getTag());
		}

		public void registerItemIcon(IIconRegister iconRegister) {
			if (this != REDSTONE)
				iconItem = iconRegister.registerIcon("buildcraft:gates/gate_material_" + getTag());
		}

		private GateMaterial(String guiFile, int guiHeight, int numSlots, boolean hasParamterSlot) {
			this.guiFile = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/" + guiFile);
			this.guiHeight = guiHeight;
			this.numSlots = numSlots;
			this.hasParameterSlot = hasParamterSlot;
		}

		public static GateMaterial fromOrdinal(int ordinal) {
			if (ordinal < 0 || ordinal >= VALUES.length)
				return REDSTONE;
			return VALUES[ordinal];
		}
	}

	public static enum GateLogic {

		AND, OR;
		public static final GateLogic[] VALUES = values();
		private IIcon iconLit;
		private IIcon iconDark;
		private IIcon iconItem;

		public IIcon getIconLit() {
			return iconLit;
		}

		public IIcon getIconDark() {
			return iconDark;
		}

		public IIcon getIconItem() {
			return iconItem;
		}

		public String getTag() {
			return name().toLowerCase(Locale.ENGLISH);
		}

		public void registerBlockIcon(IIconRegister iconRegister) {
			iconLit = iconRegister.registerIcon("buildcraft:gates/gate_lit_" + getTag());
			iconDark = iconRegister.registerIcon("buildcraft:gates/gate_dark_" + getTag());
		}

		public void registerItemIcon(IIconRegister iconRegister) {
			iconItem = iconRegister.registerIcon("buildcraft:gates/gate_logic_" + getTag());
		}

		public static GateLogic fromOrdinal(int ordinal) {
			if (ordinal < 0 || ordinal >= VALUES.length)
				return AND;
			return VALUES[ordinal];
		}
	}
}
