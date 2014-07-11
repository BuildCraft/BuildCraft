/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Achievement;
import net.minecraft.util.IIcon;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.AchievementPage;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.JavaTools;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.recipes.BuildcraftRecipes;
import buildcraft.builders.urbanism.EntityRobotUrbanism;
import buildcraft.core.BlockIndex;
import buildcraft.core.BlockSpring;
import buildcraft.core.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.ItemRobot;
import buildcraft.core.ItemSpring;
import buildcraft.core.ItemWrench;
import buildcraft.core.SpringPopulate;
import buildcraft.core.TickHandlerCoreClient;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.render.BlockHighlightHandler;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.robots.EntityRobotBuilder;
import buildcraft.core.robots.EntityRobotPicker;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.triggers.ActionMachineControl.Mode;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.core.triggers.ActionTriggerIconProvider;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.triggers.DefaultActionProvider;
import buildcraft.core.triggers.DefaultTriggerProvider;
import buildcraft.core.triggers.TriggerFluidContainer;
import buildcraft.core.triggers.TriggerFluidContainerLevel;
import buildcraft.core.triggers.TriggerInventory;
import buildcraft.core.triggers.TriggerInventoryLevel;
import buildcraft.core.triggers.TriggerMachine;
import buildcraft.core.triggers.TriggerRedstoneInput;
import buildcraft.core.utils.CraftingHandler;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.7.10,1.8)", dependencies = "required-after:Forge@[10.13.0.1179,)")
public class BuildCraftCore extends BuildCraftMod {
	public static final boolean NEXTGEN_PREALPHA = true;

	public static enum RenderMode {
		Full, NoDynamic
	}
	public static RenderMode render = RenderMode.Full;
	public static boolean debugMode = false;
	public static boolean modifyWorld = false;
	public static boolean colorBlindMode = false;
	public static boolean dropBrokenBlocks = true; // Set to false to prevent the filler from dropping broken blocks.
	public static int itemLifespan = 1200;
	public static int updateFactor = 10;
	public static long longUpdateFactor = 40;
	public static BuildCraftConfiguration mainConfiguration;

	// TODO: This doesn't seem used anymore. Remove if it's the case.
	public static TreeMap<BlockIndex, PacketUpdate> bufferedDescriptions = new TreeMap<BlockIndex, PacketUpdate>();

	public static final int trackedPassiveEntityId = 156;
	public static Block springBlock;
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;
	public static Item redstoneCrystal;
	public static Item robotBaseItem;
	public static Item robotBuilderItem;
	public static Item robotPickerItem;
	@SideOnly(Side.CLIENT)
	public static IIcon redLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon blueLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon stripesLaserTexture;
	@SideOnly(Side.CLIENT)
	public static IIcon transparentTexture;
	@SideOnly(Side.CLIENT)
	public static IIconProvider iconProvider;
	public static int blockByEntityModel;
	public static int legacyPipeModel;
	public static int markerModel;
	public static BCTrigger triggerMachineActive = new TriggerMachine(true);
	public static BCTrigger triggerMachineInactive = new TriggerMachine(false);
	public static BCTrigger triggerEmptyInventory = new TriggerInventory(TriggerInventory.State.Empty);
	public static BCTrigger triggerContainsInventory = new TriggerInventory(TriggerInventory.State.Contains);
	public static BCTrigger triggerSpaceInventory = new TriggerInventory(TriggerInventory.State.Space);
	public static BCTrigger triggerFullInventory = new TriggerInventory(TriggerInventory.State.Full);
	public static BCTrigger triggerEmptyFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Empty);
	public static BCTrigger triggerContainsFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Contains);
	public static BCTrigger triggerSpaceFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Space);
	public static BCTrigger triggerFullFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Full);
	public static BCTrigger triggerRedstoneActive = new TriggerRedstoneInput(true);
	public static BCTrigger triggerRedstoneInactive = new TriggerRedstoneInput(false);
	public static BCTrigger triggerInventoryBelow25 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_25);
	public static BCTrigger triggerInventoryBelow50 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_50);
	public static BCTrigger triggerInventoryBelow75 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_75);
	public static BCTrigger triggerFluidContainerBelow25 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_25);
	public static BCTrigger triggerFluidContainerBelow50 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_50);
	public static BCTrigger triggerFluidContainerBelow75 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_75);
	public static BCAction actionRedstone = new ActionRedstoneOutput();
	public static BCAction actionOn = new ActionMachineControl(Mode.On);
	public static BCAction actionOff = new ActionMachineControl(Mode.Off);
	public static BCAction actionLoop = new ActionMachineControl(Mode.Loop);
	public static boolean loadDefaultRecipes = true;
	public static boolean consumeWaterSources = false;
	//public static BptItem[] itemBptProps = new BptItem[Item.itemsList.length];
	@Mod.Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	public static Achievement woodenGearAchievement;
	public static Achievement stoneGearAchievement;
	public static Achievement ironGearAchievement;
	public static Achievement goldGearAchievement;
	public static Achievement diamondGearAchievement;
	public static Achievement wrenchAchievement;
	public static Achievement engineAchievement1;
	public static Achievement engineAchievement2;
	public static Achievement engineAchievement3;
	public static Achievement aLotOfCraftingAchievement;
	public static Achievement straightDownAchievement;
	public static Achievement chunkDestroyerAchievement;
	public static Achievement fasterFillingAchievement;
	public static Achievement timeForSomeLogicAchievement;
	public static Achievement refineAndRedefineAchievement;
	public static Achievement tinglyLaserAchievement;

	public static Achievement architectAchievement;
	public static Achievement builderAchievement;
	public static Achievement blueprintAchievement;
	public static Achievement templateAchievement;
	public static Achievement libraryAchievement;

	public static AchievementPage BuildcraftAchievements;

	public static HashSet<String> recipesBlacklist = new HashSet<String>();

	public static float diffX, diffY, diffZ;

	public static GameProfile gameProfile = new GameProfile(UUID.nameUUIDFromBytes("buildcraft.core".getBytes()), "[BuildCraft]");

	private static FloatBuffer modelviewF;
	private static FloatBuffer projectionF;
	private static IntBuffer viewport;

	private static FloatBuffer pos = ByteBuffer.allocateDirect(3 * 4).asFloatBuffer();

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Core");

		BCLog.initLog();

		BuildcraftRecipes.assemblyTable = AssemblyRecipeManager.INSTANCE;
		BuildcraftRecipes.integrationTable = IntegrationRecipeManager.INSTANCE;
		BuildcraftRecipes.refinery = RefineryRecipeManager.INSTANCE;

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));
		try {
			mainConfiguration.load();

			Property updateCheck = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "update.check", true);
			updateCheck.comment = "set to true for version check on startup";
			if (updateCheck.getBoolean(true)) {
				Version.check();
			}

			Property dropBlock = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "dropBrokenBlocks", true);
			dropBlock.comment = "set to false to prevent fillers from dropping blocks.";
			dropBrokenBlocks = dropBlock.getBoolean(true);

			Property lifespan = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "itemLifespan", itemLifespan);
			lifespan.comment = "the lifespan in ticks of items dropped on the ground by pipes and machines, vanilla = 6000, default = 1200";
			itemLifespan = lifespan.getInt(itemLifespan);
			if (itemLifespan < 100) {
				itemLifespan = 100;
			}

			Property factor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.updateFactor", 10);
			factor.comment = "increasing this number will decrease network update frequency, useful for overloaded servers";
			updateFactor = factor.getInt(10);

			Property longFactor = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "network.stateRefreshPeriod", 40);
			longFactor.comment = "delay between full client sync packets, increasing it saves bandwidth, decreasing makes for better client syncronization.";
			longUpdateFactor = longFactor.getInt(40);

			wrenchItem = (new ItemWrench()).setUnlocalizedName("wrenchItem");
			CoreProxy.proxy.registerItem(wrenchItem);

			Property modifyWorldProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "modifyWorld", true);
			modifyWorldProp.comment = "set to false if BuildCraft should not generate custom blocks (e.g. oil)";
			modifyWorld = modifyWorldProp.getBoolean(true);

			if (BuildCraftCore.modifyWorld) {
				BlockSpring.EnumSpring.WATER.canGen = BuildCraftCore.mainConfiguration.get("worldgen", "waterSpring", true).getBoolean(true);
				springBlock = new BlockSpring().setBlockName("eternalSpring");
				CoreProxy.proxy.registerBlock(springBlock, ItemSpring.class);
			}

			Property consumeWater = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "consumeWater", consumeWaterSources);
			consumeWaterSources = consumeWater.getBoolean(consumeWaterSources);
			consumeWater.comment = "set to true if the Pump should consume water";

			woodenGearItem = (new ItemBuildCraft()).setUnlocalizedName("woodenGearItem");
			CoreProxy.proxy.registerItem(woodenGearItem);
			OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));

			stoneGearItem = (new ItemBuildCraft()).setUnlocalizedName("stoneGearItem");
			CoreProxy.proxy.registerItem(stoneGearItem);
			OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));

			ironGearItem = (new ItemBuildCraft()).setUnlocalizedName("ironGearItem");
			CoreProxy.proxy.registerItem(ironGearItem);
			OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));

			goldGearItem = (new ItemBuildCraft()).setUnlocalizedName("goldGearItem");
			CoreProxy.proxy.registerItem(goldGearItem);
			OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));

			diamondGearItem = (new ItemBuildCraft()).setUnlocalizedName("diamondGearItem");
			CoreProxy.proxy.registerItem(diamondGearItem);
			OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));

			if (!BuildCraftCore.NEXTGEN_PREALPHA) {
				redstoneCrystal = (new ItemBuildCraft()).setUnlocalizedName("redstoneCrystal");
				CoreProxy.proxy.registerItem(redstoneCrystal);
				OreDictionary.registerOre("redstoneCrystal", new ItemStack(redstoneCrystal));

				robotBaseItem = new ItemRobot(EntityRobot.class).setUnlocalizedName("robotBase");
				CoreProxy.proxy.registerItem(robotBaseItem);

				robotPickerItem = new ItemRobot(EntityRobotPicker.class).setUnlocalizedName("robotPicker");
				CoreProxy.proxy.registerItem(robotPickerItem);

				robotBuilderItem = new ItemRobot(EntityRobotBuilder.class).setUnlocalizedName("robotBuilder");
				CoreProxy.proxy.registerItem(robotBuilderItem);
			}

			MinecraftForge.EVENT_BUS.register(this);
			MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());
		} finally {
			if (mainConfiguration.hasChanged()) {
				mainConfiguration.save();
			}
		}

	}

	@Mod.EventHandler
	public void initialize(FMLInitializationEvent evt) {
		BuildCraftAPI.proxy = CoreProxy.proxy;

		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-CORE", new BuildCraftChannelHandler(), new PacketHandler());

		ActionManager.registerTriggerProvider(new DefaultTriggerProvider());
		ActionManager.registerActionProvider(new DefaultActionProvider());

		if (BuildCraftCore.modifyWorld) {
			MinecraftForge.EVENT_BUS.register(new SpringPopulate());
		}

		for (String l : BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL,
				"recipesBlacklist", new String[0]).getStringList()) {
			recipesBlacklist.add(JavaTools.stripSurroundingQuotes(l.trim()));
		}

		if (mainConfiguration.hasChanged()) {
			mainConfiguration.save();
		}

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}
		EntityRegistry.registerModEntity(EntityRobot.class, "bcRobot", EntityIds.ROBOT, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityRobotPicker.class, "bcRobotPicker", EntityIds.ROBOT_PICKER, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityRobotBuilder.class, "bcRobotBuilder", EntityIds.ROBOT_BUILDER, instance, 50, 1, true);
		EntityRegistry.registerModEntity(EntityRobotUrbanism.class, "bcRobotUrbanism", EntityIds.ROBOT_URBANISM, instance, 50, 1, true);
		EntityList.classToStringMapping.remove(EntityRobotBuilder.class);
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcRobot");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		FMLCommonHandler.instance().bus().register(new CraftingHandler());

		CoreProxy.proxy.initializeRendering();
		CoreProxy.proxy.initializeEntityRendering();

	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		for (Object o : Block.blockRegistry) {
			Block block = (Block) o;

			if (block instanceof BlockFluidBase || block instanceof BlockLiquid || block instanceof IPlantable) {
				BuildCraftAPI.softBlocks.add(block);
			}
		}

		BuildCraftAPI.softBlocks.add(Blocks.snow);
		BuildCraftAPI.softBlocks.add(Blocks.vine);
		BuildCraftAPI.softBlocks.add(Blocks.fire);
		BuildCraftAPI.softBlocks.add(Blocks.air);

		FMLCommonHandler.instance().bus().register(new TickHandlerCoreClient());
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandBuildCraft());
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void textureHook(TextureStitchEvent.Pre event) {
		if (event.map.getTextureType() == 1) {
			iconProvider = new CoreIconProvider();
			iconProvider.registerIcons(event.map);
			ActionTriggerIconProvider.INSTANCE.registerIcons(event.map);
		} else if (event.map.getTextureType() == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraft:blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraft:blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraft:blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraft:blockTransparentLaser");
		}

	}

	public void loadRecipes() {
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', Items.iron_ingot, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(woodenGearItem), " S ", "S S", " S ", 'S', "stickWood");
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(stoneGearItem), " I ", "IGI", " I ", 'I', "cobblestone", 'G',
				woodenGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(ironGearItem), " I ", "IGI", " I ", 'I', Items.iron_ingot, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(goldGearItem), " I ", "IGI", " I ", 'I', Items.gold_ingot, 'G', ironGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', Items.diamond, 'G', goldGearItem);
	}

	@Mod.EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		InterModComms.processIMC(event);
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void renderLast (RenderWorldLastEvent evt) {
		// TODO: while the urbanist is deactivated, this code can be dormant.
		// it happens to be very expensive at run time, so we need some way
		// to operate it only when releval (e.g. in the cycle following a
		// click request).
		if (NEXTGEN_PREALPHA) {
			return;
		}

		/**
		 * Note (SpaceToad): Why on earth this thing eventually worked out is a
		 * mystery to me. In particular, all the examples I got computed y in
		 * a different way. Anyone with further OpenGL understanding would be
		 * welcome to explain.
		 *
		 * Anyway, the purpose of this code is to store the block position
		 * pointed by the mouse at each frame, relative to the entity that has
		 * the camera.
		 *
		 * It got heavily inspire from the two following sources:
		 * http://nehe.gamedev.net/article/using_gluunproject/16013/
		 * #ActiveRenderInfo.updateRenderInfo.
		 *
		 * See EntityUrbanist#rayTraceMouse for a usage example.
		 */

		if (modelviewF == null) {
			modelviewF = GLAllocation.createDirectFloatBuffer(16);
			projectionF = GLAllocation.createDirectFloatBuffer(16);
			viewport = GLAllocation.createDirectIntBuffer(16);

		}

		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelviewF);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projectionF);
		GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);
		float f = (viewport.get(0) + viewport.get(2)) / 2;
		float f1 = (viewport.get(1) + viewport.get(3)) / 2;

		float x = Mouse.getX();
		float y = Mouse.getY();

		// TODO: Minecraft seems to instist to have this winZ re-created at
		// each frame - looks like a memory leak to me but I couldn't use a
		// static variable instead, as for the rest.
		FloatBuffer winZ = GLAllocation.createDirectFloatBuffer(1);
		GL11.glReadPixels((int) x, (int) y, 1, 1, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, winZ);

		GLU.gluUnProject(x, y, winZ.get(), modelviewF, projectionF, viewport,
				pos);

		diffX = pos.get(0);
		diffY = pos.get(1);
		diffZ = pos.get(2);
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		woodenGearAchievement = new Achievement("achievement.woodenGear", "woodenGearAchievement", 0, 0, woodenGearItem, null).registerStat();
		stoneGearAchievement = new Achievement("achievement.stoneGear", "stoneGearAchievement", 2, 0, stoneGearItem, woodenGearAchievement).registerStat();
		ironGearAchievement = new Achievement("achievement.ironGear", "ironGearAchievement", 4, 0, ironGearItem, stoneGearAchievement).registerStat();
		goldGearAchievement = new Achievement("achievement.goldGear", "goldGearAchievement", 6, 0, goldGearItem, ironGearAchievement).registerStat();
		diamondGearAchievement = new Achievement("achievement.diamondGear", "diamondGearAchievement", 8, 0, diamondGearItem, goldGearAchievement).registerStat();
		wrenchAchievement = new Achievement("achievement.wrench", "wrenchAchievement", 3, 2, wrenchItem, stoneGearAchievement).registerStat();
		engineAchievement1 = new Achievement("achievement.redstoneEngine", "engineAchievement1", 1, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 0), woodenGearAchievement).registerStat();
		engineAchievement2 = new Achievement("achievement.stirlingEngine", "engineAchievement2", 3, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 1), engineAchievement1).registerStat();
		engineAchievement3 = new Achievement("achievement.combustionEngine", "engineAchievement3", 5, -2, new ItemStack(BuildCraftEnergy.engineBlock, 1, 2), engineAchievement2).registerStat();
		aLotOfCraftingAchievement = new Achievement("achievement.aLotOfCrafting", "aLotOfCraftingAchievement", 1, 2, BuildCraftFactory.autoWorkbenchBlock, woodenGearAchievement).registerStat();
		straightDownAchievement = new Achievement("achievement.straightDown", "straightDownAchievement", 5, 2, BuildCraftFactory.miningWellBlock, ironGearAchievement).registerStat();
		chunkDestroyerAchievement = new Achievement("achievement.chunkDestroyer", "chunkDestroyerAchievement", 9, 2, BuildCraftFactory.quarryBlock, diamondGearAchievement).registerStat();
		fasterFillingAchievement = new Achievement("achievement.fasterFilling", "fasterFillingAchievement", 7, 2, BuildCraftBuilders.fillerBlock, goldGearAchievement).registerStat();
		timeForSomeLogicAchievement = new Achievement("achievement.timeForSomeLogic", "timeForSomeLogicAchievement", 9, -2, BuildCraftSilicon.assemblyTableBlock, diamondGearAchievement).registerStat();
		refineAndRedefineAchievement = new Achievement("achievement.refineAndRedefine", "refineAndRedefineAchievement", 10, 0, BuildCraftFactory.refineryBlock, diamondGearAchievement).registerStat();
		tinglyLaserAchievement = new Achievement("achievement.tinglyLaser", "tinglyLaserAchievement", 11, -2, BuildCraftSilicon.laserBlock,
				timeForSomeLogicAchievement).registerStat();
		architectAchievement = new Achievement("achievement.architect", "architectAchievement", 11, 2, BuildCraftBuilders.architectBlock, chunkDestroyerAchievement).registerStat();
		builderAchievement = new Achievement("achievement.builder", "builderAchievement", 13, 2, BuildCraftBuilders.builderBlock, architectAchievement).registerStat();
        blueprintAchievement = new Achievement("achievement.blueprint", "blueprintAchievement", 11, 4, BuildCraftBuilders.blueprintItem, architectAchievement).registerStat();
        templateAchievement = new Achievement("achievement.template", "templateAchievement", 13, 4, BuildCraftBuilders.templateItem, blueprintAchievement).registerStat();
        libraryAchievement = new Achievement("achievement.blueprintLibrary", "blueprintLibraryAchievement", 15, 2, BuildCraftBuilders.libraryBlock, builderAchievement).registerStat();

		BuildcraftAchievements = new AchievementPage("Buildcraft", woodenGearAchievement, stoneGearAchievement, ironGearAchievement, goldGearAchievement, diamondGearAchievement, wrenchAchievement, engineAchievement1, engineAchievement2, engineAchievement3, aLotOfCraftingAchievement, straightDownAchievement, chunkDestroyerAchievement, fasterFillingAchievement, timeForSomeLogicAchievement, refineAndRedefineAchievement, tinglyLaserAchievement, architectAchievement, builderAchievement, blueprintAchievement, templateAchievement, libraryAchievement);
		AchievementPage.registerAchievementPage(BuildcraftAchievements);
	}
}
