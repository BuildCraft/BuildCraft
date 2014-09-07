package buildcraft;

import buildcraft.core.Version;
import buildcraft.research.BasicEurekaChapter;
import buildcraft.research.CraftingHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import eureka.api.EurekaInfo;
import eureka.api.EurekaRegistry;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Research", acceptedMinecraftVersions = "[1.7.10,1.8)", dependencies = "required-after:Forge@[10.13.0.1179,)")
public class BuildcraftResearch extends BuildCraftMod {

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		FMLCommonHandler.instance().bus().register(new CraftingHandler());

		EurekaRegistry.registerCategory("Buildcraft|Automatization", new ItemStack(BuildCraftFactory.quarryBlock));

		EurekaRegistry.register(new EurekaInfo("autoWorkbench", "Buildcraft|Automatization", 1, 50, new ItemStack(BuildCraftFactory.autoWorkbenchBlock), new BasicEurekaChapter("autoWorkbench", false)));

		EurekaRegistry.register(new EurekaInfo("tank", "Buildcraft|Automatization", 1, 10, new ItemStack(BuildCraftFactory.tankBlock), new BasicEurekaChapter("tank", false)));

		ArrayList<String> requiredResearch = new ArrayList<String>();

		EurekaRegistry.register(new EurekaInfo("miningWell", "Buildcraft|Automatization", 1, 200, new ItemStack(BuildCraftFactory.miningWellBlock), new BasicEurekaChapter("miningWell", false)));

		requiredResearch.add("tank");
		requiredResearch.add("miningWell");
		EurekaRegistry.register(new EurekaInfo("pump", "Buildcraft|Automatization", 1, 6, new ItemStack(BuildCraftFactory.pumpBlock), new BasicEurekaChapter("pump", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("tank");
		requiredResearch.add("pump");
		EurekaRegistry.register(new EurekaInfo("floodgate", "Buildcraft|Automatization", 1, 6, new ItemStack(BuildCraftFactory.floodGateBlock), new BasicEurekaChapter("floodgate", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("miningWell");
		EurekaRegistry.register(new EurekaInfo("quarry", "Buildcraft|Automatization", 1, 5, new ItemStack(BuildCraftFactory.quarryBlock), new BasicEurekaChapter("quarry", true), requiredResearch));
		requiredResearch.clear();

		EurekaRegistry.register(new EurekaInfo("refinery", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftFactory.refineryBlock), new BasicEurekaChapter("refinery", false)));

		EurekaRegistry.register(new EurekaInfo("filteredBuffer", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftTransport.filteredBufferBlock), new BasicEurekaChapter("filteredBuffer", false)));

		EurekaRegistry.register(new EurekaInfo("chute", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftFactory.hopperBlock), new BasicEurekaChapter("chute", false)));

		EurekaRegistry.registerCategory("Buildcraft|TransportPipes", new ItemStack(BuildCraftTransport.pipeItemsDiamond));

		EurekaRegistry.register(new EurekaInfo("woodItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsWood), new BasicEurekaChapter("woodItems", false)));

		EurekaRegistry.register(new EurekaInfo("cobblestoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsCobblestone), new BasicEurekaChapter("cobblestoneItems", false)));

		requiredResearch.add("cobbleStoneItems");
		EurekaRegistry.register(new EurekaInfo ("stoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsStone), new BasicEurekaChapter("stoneItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("quartzItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsQuartz), new BasicEurekaChapter("quartzItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("sandstoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsSandstone), new BasicEurekaChapter("sandstoneItems", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("stoneItems");
		EurekaRegistry.register(new EurekaInfo("goldItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsGold), new BasicEurekaChapter("goldItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("ironItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsIron), new BasicEurekaChapter("ironItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("voidItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsVoid), new BasicEurekaChapter("voidItems", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("woodItems");
		EurekaRegistry.register(new EurekaInfo("obsidianItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsObsidian), new BasicEurekaChapter("obsidianItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("emeraldItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsEmerald), new BasicEurekaChapter("emeraldItems", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("ironItems");
		EurekaRegistry.register(new EurekaInfo("diamondItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsDiamond), new BasicEurekaChapter("diamondItems", true), requiredResearch));
		requiredResearch.clear();

		requiredResearch.add("diamondItems");
		EurekaRegistry.register(new EurekaInfo("lazuliItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsLapis), new BasicEurekaChapter("lazuliItems", true), requiredResearch));

		EurekaRegistry.register(new EurekaInfo("diazuliItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsDaizuli), new BasicEurekaChapter("diazuliItems", true), requiredResearch));
		requiredResearch.clear();

	}
}
