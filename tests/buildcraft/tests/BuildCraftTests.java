/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import buildcraft.BuildCraftMod;
import buildcraft.core.DefaultProps;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;

@Mod(name = "BuildCraft Tests", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Tests", dependencies = DefaultProps.DEPENDENCY_CORE)
public class BuildCraftTests extends BuildCraftMod {

	public static Block blockTestPathfinding;

	public static Item tester;

	@Mod.Instance("BuildCraft|Tests")
	public static BuildCraftTests instance;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		blockTestPathfinding = new BlockTestPathfinding();
		CoreProxy.proxy.registerBlock(blockTestPathfinding);
		CoreProxy.proxy.registerTileEntity(TileTestPathfinding.class, "net.minecraft.src.builders.TileTestPathfinding");

		tester = new ItemTester();
		tester.setUnlocalizedName("tester");
		CoreProxy.proxy.registerItem(tester);
	}
}
