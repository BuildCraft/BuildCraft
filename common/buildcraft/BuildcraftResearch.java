package buildcraft;

import buildcraft.core.Version;
import buildcraft.research.BasicEurekaChapter;
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
		EurekaRegistry.registerCategory("Buildcraft", new ItemStack(BuildCraftCore.wrenchItem));
		EurekaRegistry.register(new EurekaInfo("quarry", "Buildcraft", 1, 5, new ItemStack(BuildCraftFactory.quarryBlock), new BasicEurekaChapter("quarry", true)));
		EurekaRegistry.register(new EurekaInfo("tank", "Buildcraft", 1, 10, new ItemStack(BuildCraftFactory.tankBlock), new BasicEurekaChapter("tank", false)));
	}
}
