/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import cpw.mods.fml.common.network.IGuiHandler;

import buildcraft.tests.testcase.ContainerTestCase;
import buildcraft.tests.testcase.GuiTestCase;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
		case GuiTestIds.TESTER_ID:
			return new GuiTester(player, x, y, z);
		case GuiTestIds.TESTCASE_ID:
			return new GuiTestCase(player, x, y, z);
		default:
			return null;
		}

	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
		switch (id) {
		case GuiTestIds.TESTER_ID:
			return new ContainerTester(player, x, y, z);
		case GuiTestIds.TESTCASE_ID:
			return new ContainerTestCase(player, x, y, z);
		default:
			return null;
		}

	}

}
