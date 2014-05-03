/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.triggers;

import java.util.LinkedList;

import org.apache.logging.log4j.Level;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.common.FMLLog;

import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionProvider;
import buildcraft.core.IMachine;

public class DefaultActionProvider implements IActionProvider {

	@Override
	public LinkedList<IAction> getNeighborActions(Block block, TileEntity tile) {
		LinkedList<IAction> res = new LinkedList<IAction>();

		res.add(BuildCraftCore.actionRedstone);

		try {
			if (tile instanceof IMachine) {
				IMachine machine = (IMachine) tile;
				if (machine.allowAction(BuildCraftCore.actionOn)) {
					res.add(BuildCraftCore.actionOn);
				}
				if (machine.allowAction(BuildCraftCore.actionOff)) {
					res.add(BuildCraftCore.actionOff);
				}
				if (machine.allowAction(BuildCraftCore.actionLoop)) {
					res.add(BuildCraftCore.actionLoop);
				}
			}
		} catch (Throwable error) {
			FMLLog.log("Buildcraft", Level.FATAL, "Outdated API detected, please update your mods!");
		}

		return res;
	}
}
