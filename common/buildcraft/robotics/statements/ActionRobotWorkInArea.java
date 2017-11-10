/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.statements;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;

import buildcraft.api.core.IZone;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;

public class ActionRobotWorkInArea extends BCStatement implements IActionInternal {

	public enum AreaType {
		WORK("work_in_area"),
		LOAD_UNLOAD("load_unload_area");

		private final String name;

		AreaType(String iName) {
			name = iName;
		}

		public String getTag() {
			return "buildcraft:robot." + name;
		}

		public String getUnlocalizedName() {
			return "gate.action.robot." + name;
		}

		public String getIcon() {
			return "buildcraftrobotics:triggers/action_robot_" + name;
		}
	}

	private final AreaType areaType;

	public ActionRobotWorkInArea(AreaType iAreaType) {
		super(iAreaType.getTag());

		areaType = iAreaType;
	}

	@Override
	public String getDescription() {
		return StringUtils.localize(areaType.getUnlocalizedName());
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		icon = iconRegister.registerIcon(areaType.getIcon());
	}

	public static IZone getArea(StatementSlot slot) {
		if (slot.parameters[0] == null) {
			return null;
		}

		ItemStack stack = slot.parameters[0].getItemStack();

		if (stack == null || !(stack.getItem() instanceof IMapLocation)) {
			return null;
		}

		IMapLocation map = (IMapLocation) stack.getItem();
		return map.getZone(stack);
	}

	@Override
	public int minParameters() {
		return 1;
	}

	@Override
	public int maxParameters() {
		return 1;
	}

	@Override
	public IStatementParameter createParameter(int index) {
		return new StatementParameterMapLocation();
	}

	@Override
	public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {
	}

	public AreaType getAreaType() {
		return areaType;
	}
}
