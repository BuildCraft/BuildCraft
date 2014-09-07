package buildcraft;

import buildcraft.core.Version;
import eureka.api.BasicEurekaChapter;
import buildcraft.research.CraftingHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import eureka.api.EurekaInfo;
import eureka.api.EurekaRegistry;
import net.minecraft.item.ItemStack;

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
		//Automatization
		EurekaRegistry.registerCategory("Buildcraft|Automatization", new ItemStack(BuildCraftFactory.quarryBlock));

		EurekaRegistry.register(new EurekaInfo("autoWorkbench", "Buildcraft|Automatization", 1, 50, new ItemStack(BuildCraftFactory.autoWorkbenchBlock)));

		EurekaRegistry.register(new EurekaInfo("tank", "Buildcraft|Automatization", 1, 10, new ItemStack(BuildCraftFactory.tankBlock)));

		EurekaRegistry.register(new EurekaInfo("miningWell", "Buildcraft|Automatization", 1, 200, new ItemStack(BuildCraftFactory.miningWellBlock), new BasicEurekaChapter("miningWell", false)));

		EurekaRegistry.register(new EurekaInfo("pump", "Buildcraft|Automatization", 1, 6, new ItemStack(BuildCraftFactory.pumpBlock), "tank", "miningWell"));

		EurekaRegistry.register(new EurekaInfo("floodgate", "Buildcraft|Automatization", 1, 6, new ItemStack(BuildCraftFactory.floodGateBlock), "tank", "pump"));

		EurekaRegistry.register(new EurekaInfo("quarry", "Buildcraft|Automatization", 1, 5, new ItemStack(BuildCraftFactory.quarryBlock), "miningWell"));

		EurekaRegistry.register(new EurekaInfo("refinery", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftFactory.refineryBlock)));

		EurekaRegistry.register(new EurekaInfo("filteredBuffer", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftTransport.filteredBufferBlock)));

		EurekaRegistry.register(new EurekaInfo("chute", "Buildcraft|Automatization", 1, 1, new ItemStack(BuildCraftFactory.hopperBlock)));

		//Transport Pipes
		EurekaRegistry.registerCategory("Buildcraft|TransportPipes", new ItemStack(BuildCraftTransport.pipeItemsDiamond));

		EurekaRegistry.register(new EurekaInfo("woodItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsWood)));

		EurekaRegistry.register(new EurekaInfo("cobblestoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsCobblestone)));


		EurekaRegistry.register(new EurekaInfo ("stoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsStone), "cobbleStoneItems"));

		EurekaRegistry.register(new EurekaInfo("quartzItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsQuartz), "cobbleStoneItems"));

		EurekaRegistry.register(new EurekaInfo("sandstoneItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsSandstone), "cobbleStoneItems"));

		EurekaRegistry.register(new EurekaInfo("goldItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsGold), "stoneItems"));

		EurekaRegistry.register(new EurekaInfo("ironItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsIron), "stoneItems"));

		EurekaRegistry.register(new EurekaInfo("voidItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsVoid), "stoneItems"));

		EurekaRegistry.register(new EurekaInfo("obsidianItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsObsidian), "woodItems"));

		EurekaRegistry.register(new EurekaInfo("emeraldItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsEmerald), "woodItems"));

		EurekaRegistry.register(new EurekaInfo("diamondItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsDiamond), "ironItems"));

		EurekaRegistry.register(new EurekaInfo("lazuliItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsLapis), "diamondItems"));

		EurekaRegistry.register(new EurekaInfo("diazuliItems", "Buildcraft|TransportPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeItemsDaizuli), "diamondItems"));

		//Fluid pipes
		EurekaRegistry.registerCategory("Buildcraft|FluidPipes", new ItemStack(BuildCraftTransport.pipeFluidsEmerald));

		EurekaRegistry.register(new EurekaInfo("woodenFluid", "Buildcraft|FluidPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeFluidsWood), "woodItems"));

		EurekaRegistry.register(new EurekaInfo("cobblestoneFluid", "Buildcraft|FluidPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeFluidsCobblestone), "woodFluid", "cobbleStoneItems"));

		EurekaRegistry.register(new EurekaInfo("stoneFluid", "Buildcraft|FluidPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeFluidsStone), "cobblestoneFluid", "stoneItems"));

		EurekaRegistry.register(new EurekaInfo("goldFluid", "Buildcraft|FluidPipes", 1, 1, new ItemStack(BuildCraftTransport.pipeFluidsGold), "stoneFluid", "goldItems"));


	}
}
