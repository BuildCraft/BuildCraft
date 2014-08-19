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
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.blueprints.SchematicRegistry;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.JavaTools;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.recipes.BuildcraftRecipeRegistry;
import buildcraft.core.BlockSpring;
import buildcraft.core.configuration.BuildCraftConfiguration;
import buildcraft.core.CommandBuildCraft;
import buildcraft.core.configuration.ConfigHandeler;
import buildcraft.core.CoreIconProvider;
import buildcraft.core.DefaultProps;
import buildcraft.core.GuiHandler;
import buildcraft.core.InterModComms;
import buildcraft.core.ItemGear;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.ItemScienceBook;
import buildcraft.core.ItemSpring;
import buildcraft.core.ItemWrench;
import buildcraft.core.SpringPopulate;
import buildcraft.core.TickHandlerCore;
import buildcraft.core.Version;
import buildcraft.core.network.BuildCraftChannelHandler;
import buildcraft.core.network.EntityIds;
import buildcraft.core.network.NetworkIdRegistry;
import buildcraft.core.network.PacketHandler;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.recipes.IntegrationRecipeManager;
import buildcraft.core.recipes.RefineryRecipeManager;
import buildcraft.core.render.BlockHighlightHandler;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.science.TechnoField;
import buildcraft.core.science.TechnoSimpleItem;
import buildcraft.core.science.TechnoStatement;
import buildcraft.core.science.Technology;
import buildcraft.core.science.Tier;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.triggers.ActionMachineControl.Mode;
import buildcraft.core.triggers.ActionRedstoneOutput;
import buildcraft.core.triggers.DefaultActionProvider;
import buildcraft.core.triggers.DefaultTriggerProvider;
import buildcraft.core.triggers.StatementIconProvider;
import buildcraft.core.triggers.TriggerEnergy;
import buildcraft.core.triggers.TriggerFluidContainer;
import buildcraft.core.triggers.TriggerFluidContainerLevel;
import buildcraft.core.triggers.TriggerInventory;
import buildcraft.core.triggers.TriggerInventoryLevel;
import buildcraft.core.triggers.TriggerMachine;
import buildcraft.core.triggers.TriggerRedstoneInput;
import buildcraft.core.utils.EventHandeler;
import buildcraft.core.utils.WorldPropertyIsDirt;
import buildcraft.core.utils.WorldPropertyIsFarmland;
import buildcraft.core.utils.WorldPropertyIsFluidSource;
import buildcraft.core.utils.WorldPropertyIsHarvestable;
import buildcraft.core.utils.WorldPropertyIsLeave;
import buildcraft.core.utils.WorldPropertyIsOre;
import buildcraft.core.utils.WorldPropertyIsShoveled;
import buildcraft.core.utils.WorldPropertyIsSoft;
import buildcraft.core.utils.WorldPropertyIsWood;
import buildcraft.silicon.ItemRedstoneChipset.Chipset;

@Mod(name = "BuildCraft", version = Version.VERSION, useMetadata = false, modid = "BuildCraft|Core", acceptedMinecraftVersions = "[1.7.10,1.8)", dependencies = "required-after:Forge@[10.13.0.1179,)", guiFactory = "buildcraft.core.configuration.GuiFactory")
public class BuildCraftCore extends BuildCraftMod {

	@Mod.Instance("BuildCraft|Core")
	public static BuildCraftCore instance;

	public static final boolean NONRELEASED_BLOCKS = true;

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
	public static Item scienceBookItem;
	public static Item woodenGearItem;
	public static Item stoneGearItem;
	public static Item ironGearItem;
	public static Item goldGearItem;
	public static Item diamondGearItem;
	public static Item wrenchItem;
	public static Item mapLocationItem;
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
	public static ITrigger triggerMachineActive = new TriggerMachine(true);
	public static ITrigger triggerMachineInactive = new TriggerMachine(false);
	public static ITrigger triggerEnergyHigh = new TriggerEnergy(true);
	public static ITrigger triggerEnergyLow = new TriggerEnergy(false);
	public static ITrigger triggerEmptyInventory = new TriggerInventory(TriggerInventory.State.Empty);
	public static ITrigger triggerContainsInventory = new TriggerInventory(TriggerInventory.State.Contains);
	public static ITrigger triggerSpaceInventory = new TriggerInventory(TriggerInventory.State.Space);
	public static ITrigger triggerFullInventory = new TriggerInventory(TriggerInventory.State.Full);
	public static ITrigger triggerEmptyFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Empty);
	public static ITrigger triggerContainsFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Contains);
	public static ITrigger triggerSpaceFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Space);
	public static ITrigger triggerFullFluid = new TriggerFluidContainer(TriggerFluidContainer.State.Full);
	public static ITrigger triggerRedstoneActive = new TriggerRedstoneInput(true);
	public static ITrigger triggerRedstoneInactive = new TriggerRedstoneInput(false);
	public static ITrigger triggerInventoryBelow25 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_25);
	public static ITrigger triggerInventoryBelow50 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_50);
	public static ITrigger triggerInventoryBelow75 = new TriggerInventoryLevel(TriggerInventoryLevel.TriggerType.BELOW_75);
	public static ITrigger triggerFluidContainerBelow25 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_25);
	public static ITrigger triggerFluidContainerBelow50 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_50);
	public static ITrigger triggerFluidContainerBelow75 = new TriggerFluidContainerLevel(TriggerFluidContainerLevel.TriggerType.BELOW_75);
	public static IAction actionRedstone = new ActionRedstoneOutput();
	public static IAction actionOn = new ActionMachineControl(Mode.On);
	public static IAction actionOff = new ActionMachineControl(Mode.Off);
	public static IAction actionLoop = new ActionMachineControl(Mode.Loop);

	public static boolean loadDefaultRecipes = true;
	public static boolean consumeWaterSources = false;

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

	public static TechnoField technoTransport = new TechnoField();
	public static TechnoField technoEnergy = new TechnoField();
	public static TechnoField technoLiquid = new TechnoField();
	public static TechnoField technoCrafting = new TechnoField();
	public static TechnoField technoMining = new TechnoField();
	public static TechnoField technoBuilding = new TechnoField();
	public static TechnoField technoSilicon = new TechnoField();
	public static TechnoField technoRobotics = new TechnoField();
	public static TechnoField technoCommander = new TechnoField();

	public static TechnoSimpleItem technoWrenchItem = new TechnoSimpleItem();
	public static TechnoSimpleItem technoMapLocation = new TechnoSimpleItem();

	public static TechnoStatement technoTriggerMachineActive = new TechnoStatement();
	public static TechnoStatement technoTriggerEnergyHigh = new TechnoStatement();
	public static TechnoStatement technoTriggerContainsInventory = new TechnoStatement();
	public static TechnoStatement technoTriggerContainsFluid = new TechnoStatement();
	public static TechnoStatement technoTriggerRedstoneActive = new TechnoStatement();
	public static TechnoStatement technoTriggerInventoryBelow25 = new TechnoStatement();
	public static TechnoStatement technoTriggerFluidContainerBelow25 = new TechnoStatement();
	public static TechnoStatement technoActionRedstone = new TechnoStatement();
	public static TechnoStatement technoActionOn = new TechnoStatement();

	private static FloatBuffer modelviewF;
	private static FloatBuffer projectionF;
	private static IntBuffer viewport;

	private static FloatBuffer pos = ByteBuffer.allocateDirect(3 * 4).asFloatBuffer();

	@Mod.EventHandler
	public void loadConfiguration(FMLPreInitializationEvent evt) {
		SchematicRegistry.declareBlueprintSupport("BuildCraft|Core");

		BCLog.initLog();

		BuildcraftRecipeRegistry.assemblyTable = AssemblyRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.integrationTable = IntegrationRecipeManager.INSTANCE;
		BuildcraftRecipeRegistry.refinery = RefineryRecipeManager.INSTANCE;

		mainConfiguration = new BuildCraftConfiguration(new File(evt.getModConfigurationDirectory(), "buildcraft/main.conf"));

        ConfigHandeler.readConfiguration();

        if (BuildCraftCore.modifyWorld) {
            BlockSpring.EnumSpring.WATER.canGen = BuildCraftCore.mainConfiguration.get("worldgen", "waterSpring", true).getBoolean(true);
            BuildCraftCore.springBlock = new BlockSpring().setBlockName("eternalSpring");
            CoreProxy.proxy.registerBlock(BuildCraftCore.springBlock, ItemSpring.class);
        }

        wrenchItem = (new ItemWrench()).setUnlocalizedName("wrenchItem");
        CoreProxy.proxy.registerItem(wrenchItem);

        mapLocationItem = (new ItemMapLocation()).setUnlocalizedName("mapLocation");
        CoreProxy.proxy.registerItem(mapLocationItem);

			if (!NONRELEASED_BLOCKS) {
				scienceBookItem = (new ItemScienceBook()).setUnlocalizedName("scienceBook");
				CoreProxy.proxy.registerItem(scienceBookItem);
			}

			woodenGearItem = (new ItemGear(10 * 20)).setUnlocalizedName("woodenGearItem");
			CoreProxy.proxy.registerItem(woodenGearItem);
			OreDictionary.registerOre("gearWood", new ItemStack(woodenGearItem));

			stoneGearItem = (new ItemGear(20 * 20)).setUnlocalizedName("stoneGearItem");
			CoreProxy.proxy.registerItem(stoneGearItem);
			OreDictionary.registerOre("gearStone", new ItemStack(stoneGearItem));

			ironGearItem = (new ItemGear(40 * 20)).setUnlocalizedName("ironGearItem");
			CoreProxy.proxy.registerItem(ironGearItem);
			OreDictionary.registerOre("gearIron", new ItemStack(ironGearItem));

			goldGearItem = (new ItemGear(80 * 20)).setUnlocalizedName("goldGearItem");
			CoreProxy.proxy.registerItem(goldGearItem);
			OreDictionary.registerOre("gearGold", new ItemStack(goldGearItem));

			diamondGearItem = (new ItemGear(160 * 20)).setUnlocalizedName("diamondGearItem");
			CoreProxy.proxy.registerItem(diamondGearItem);
			OreDictionary.registerOre("gearDiamond", new ItemStack(diamondGearItem));

			MinecraftForge.EVENT_BUS.register(this);
			MinecraftForge.EVENT_BUS.register(new BlockHighlightHandler());



	}

	@Mod.EventHandler
	public void initialize(FMLInitializationEvent evt) {
		BuildCraftAPI.proxy = CoreProxy.proxy;

		channels = NetworkRegistry.INSTANCE.newChannel
				(DefaultProps.NET_CHANNEL_NAME + "-CORE", new BuildCraftChannelHandler(), new PacketHandler());

		NetworkIdRegistry.instance = new NetworkIdRegistry();

		StatementManager.registerTriggerProvider(new DefaultTriggerProvider());
		StatementManager.registerActionProvider(new DefaultActionProvider());

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
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcLaser");
		EntityList.stringToClassMapping.remove("BuildCraft|Core.bcEnergyLaser");

		FMLCommonHandler.instance().bus().register(new EventHandeler());

		CoreProxy.proxy.initializeRendering();
		CoreProxy.proxy.initializeEntityRendering();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
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

		FMLCommonHandler.instance().bus().register(new TickHandlerCore());

		BuildCraftAPI.isSoftProperty = new WorldPropertyIsSoft();
		BuildCraftAPI.isWoodProperty = new WorldPropertyIsWood();
		BuildCraftAPI.isLeavesProperty = new WorldPropertyIsLeave();
		BuildCraftAPI.isBasicOreProperty = new WorldPropertyIsOre(false);
		BuildCraftAPI.isExtendedOreProperty = new WorldPropertyIsOre(true);
		BuildCraftAPI.isHarvestableProperty = new WorldPropertyIsHarvestable();
		BuildCraftAPI.isFarmlandProperty = new WorldPropertyIsFarmland();
		BuildCraftAPI.isShoveled = new WorldPropertyIsShoveled();
		BuildCraftAPI.isDirtProperty = new WorldPropertyIsDirt();
		BuildCraftAPI.isFluidSource = new WorldPropertyIsFluidSource();
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
			StatementIconProvider.INSTANCE.registerIcons(event.map);
			EnumColor.registerIcons(event.map);

			for (Technology t : Technology.technologies.values()) {
				t.registerIcons(event.map);
			}
		} else if (event.map.getTextureType() == 0) {
			BuildCraftCore.redLaserTexture = event.map.registerIcon("buildcraft:blockRedLaser");
			BuildCraftCore.blueLaserTexture = event.map.registerIcon("buildcraft:blockBlueLaser");
			BuildCraftCore.stripesLaserTexture = event.map.registerIcon("buildcraft:blockStripesLaser");
			BuildCraftCore.transparentTexture = event.map.registerIcon("buildcraft:blockTransparentLaser");
		}

	}

	@Mod.EventHandler
	public void loadTechnology(FMLPostInitializationEvent evt) {
		Tier.initializeTechnologies();

		// Technology Clusters

		technoTransport.initialize(
				Tier.WoodenGear,
				"buildcraft:unknown",
				"technology.field.Transport",
				new ItemStack(woodenGearItem, 15));

		technoEnergy.initialize(
				Tier.WoodenGear,
				"buildcraft:unknown",
				"technology.field.Energy",
				new ItemStack(woodenGearItem, 15));

		technoLiquid.initialize(
				Tier.WoodenGear,
				"buildcraft:unknown",
				"technology.field.Liquid",
				new ItemStack(woodenGearItem, 15));

		technoCrafting.initialize(
				Tier.WoodenGear,
				"buildcraft:unknown",
				"technology.field.Crafting",
				new ItemStack(woodenGearItem, 15));

		technoMining.initialize(
				Tier.IronGear,
				"buildcraft:unknown",
				"technology.field.Mining",
				new ItemStack(woodenGearItem, 15));

		technoBuilding.initialize(
				Tier.GoldenGear,
				"buildcraft:unknown",
				"technology.field.Building",
				new ItemStack(woodenGearItem, 15));

		technoSilicon.initialize(
				Tier.RedstoneCrystalGear,
				"buildcraft:unknown",
				"technology.field.Silicon",
				new ItemStack(woodenGearItem, 15));

		technoRobotics.initialize(
				Tier.DiamondChipset,
				"buildcraft:unknown",
				"technology.field.Robotics",
				new ItemStack(woodenGearItem, 15));

		technoCommander.initialize(
				Tier.RedstoneCrystalChipset,
				"buildcraft:unknown",
				"technology.field.Commander",
				new ItemStack(woodenGearItem, 15));

		// Items

		technoWrenchItem.initialize(
				Tier.StoneGear,
				wrenchItem,
				new ItemStack(stoneGearItem, 10));

		technoMapLocation.initialize(
				Tier.DiamondChipset,
				mapLocationItem,
				new ItemStack(stoneGearItem, 10),
				technoRobotics);

		// Statements

		technoTriggerMachineActive.initialize(
				Tier.Chipset,
				triggerMachineActive,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerEnergyHigh.initialize(
				Tier.Chipset,
				triggerEnergyHigh,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerContainsInventory.initialize(
				Tier.Chipset,
				triggerContainsInventory,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerContainsFluid.initialize(
				Tier.Chipset,
				triggerContainsFluid,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerRedstoneActive.initialize(
				Tier.Chipset,
				triggerRedstoneActive,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerInventoryBelow25.initialize(
				Tier.Chipset,
				triggerInventoryBelow25,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoTriggerFluidContainerBelow25.initialize(
				Tier.Chipset,
				triggerFluidContainerBelow25,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoActionRedstone.initialize(
				Tier.Chipset,
				actionRedstone,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);

		technoActionOn.initialize(
				Tier.Chipset,
				actionOn,
				"",
				Chipset.RED.getStack(5),
				technoSilicon);
	}

	public void loadRecipes() {
		if (!NONRELEASED_BLOCKS) {
			CoreProxy.proxy.addCraftingRecipe(new ItemStack(scienceBookItem), "R ", "B ", 'R', Blocks.redstone_torch, 'B',
					Items.book);
		}

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(wrenchItem), "I I", " G ", " I ", 'I', Items.iron_ingot, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(Tier.WoodenGear.getTechnology(), new ItemStack(woodenGearItem), " S ", "S S",
				" S ", 'S',
				"stickWood");
		CoreProxy.proxy.addCraftingRecipe(Tier.StoneGear.getTechnology(), new ItemStack(stoneGearItem), " I ", "IGI",
				" I ", 'I',
				"cobblestone", 'G',
				woodenGearItem);
		CoreProxy.proxy.addCraftingRecipe(Tier.IronGear.getTechnology(), new ItemStack(ironGearItem), " I ", "IGI",
				" I ", 'I',
				Items.iron_ingot, 'G', stoneGearItem);
		CoreProxy.proxy.addCraftingRecipe(Tier.GoldenGear.getTechnology(), new ItemStack(goldGearItem), " I ", "IGI",
				" I ", 'I',
				Items.gold_ingot, 'G', ironGearItem);
		CoreProxy.proxy.addCraftingRecipe(Tier.DiamondGear.getTechnology(),
				new ItemStack(diamondGearItem), " I ", "IGI", " I ", 'I', Items.diamond, 'G', goldGearItem);
		CoreProxy.proxy.addCraftingRecipe(new ItemStack(mapLocationItem), "ppp", "pYp", "ppp", 'p', Items.paper, 'Y', new ItemStack(Items.dye, 1, 11));
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
		if (NONRELEASED_BLOCKS) {
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

	@SubscribeEvent
	public void cleanRegistries(WorldEvent.Unload unload) {
		BuildCraftAPI.isSoftProperty.clear();
		BuildCraftAPI.isWoodProperty.clear();
		BuildCraftAPI.isLeavesProperty.clear();
		BuildCraftAPI.isBasicOreProperty.clear();
		BuildCraftAPI.isExtendedOreProperty.clear();
		BuildCraftAPI.isHarvestableProperty.clear();
		BuildCraftAPI.isFarmlandProperty.clear();
		BuildCraftAPI.isShoveled.clear();
		BuildCraftAPI.isDirtProperty.clear();
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
