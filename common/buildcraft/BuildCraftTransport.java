/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile or
 * run the code. It does *NOT* grant the right to redistribute this software or
 * its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */
package buildcraft;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.recipes.AssemblyRecipe;
import buildcraft.api.transport.IExtractionHandler;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.Version;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.BCAction;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.core.utils.EnumColor;
import buildcraft.transport.BlockFilteredBuffer;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.GateIconProvider;
import buildcraft.transport.GuiHandler;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemGate;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.ItemPlug;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTriggerProvider;
import buildcraft.transport.TransportProxy;
import buildcraft.transport.WireIconProvider;
import buildcraft.transport.blueprints.BptBlockPipe;
import buildcraft.transport.blueprints.BptItemPipeDiamond;
import buildcraft.transport.blueprints.BptItemPipeEmerald;
import buildcraft.transport.blueprints.BptItemPipeIron;
import buildcraft.transport.blueprints.BptItemPipeWooden;
import buildcraft.transport.network.PacketHandlerTransport;
import buildcraft.transport.pipes.PipeFluidsCobblestone;
import buildcraft.transport.pipes.PipeFluidsEmerald;
import buildcraft.transport.pipes.PipeFluidsGold;
import buildcraft.transport.pipes.PipeFluidsIron;
import buildcraft.transport.pipes.PipeFluidsQuartz;
import buildcraft.transport.pipes.PipeFluidsSandstone;
import buildcraft.transport.pipes.PipeFluidsStone;
import buildcraft.transport.pipes.PipeFluidsVoid;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsCobblestone;
import buildcraft.transport.pipes.PipeItemsDaizuli;
import buildcraft.transport.pipes.PipeItemsDiamond;
import buildcraft.transport.pipes.PipeItemsEmerald;
import buildcraft.transport.pipes.PipeItemsGold;
import buildcraft.transport.pipes.PipeItemsIron;
import buildcraft.transport.pipes.PipeItemsLapis;
import buildcraft.transport.pipes.PipeItemsObsidian;
import buildcraft.transport.pipes.PipeItemsQuartz;
import buildcraft.transport.pipes.PipeItemsSandstone;
import buildcraft.transport.pipes.PipeItemsStone;
import buildcraft.transport.pipes.PipeItemsVoid;
import buildcraft.transport.pipes.PipeItemsWood;
import buildcraft.transport.pipes.PipePowerCobblestone;
import buildcraft.transport.pipes.PipePowerDiamond;
import buildcraft.transport.pipes.PipePowerGold;
import buildcraft.transport.pipes.PipePowerQuartz;
import buildcraft.transport.pipes.PipePowerStone;
import buildcraft.transport.pipes.PipePowerWood;
import buildcraft.transport.pipes.PipeStructureCobblestone;
import buildcraft.transport.triggers.ActionEnergyPulser;
import buildcraft.transport.triggers.ActionPipeColor;
import buildcraft.transport.triggers.ActionPipeDirection;
import buildcraft.transport.triggers.ActionSignalOutput;
import buildcraft.transport.triggers.ActionSingleEnergyPulse;
import buildcraft.transport.triggers.TriggerFilteredBufferInventoryLevel;
import buildcraft.transport.triggers.TriggerPipeContents;
import buildcraft.transport.triggers.TriggerPipeContents.Kind;
import buildcraft.transport.triggers.TriggerPipeSignal;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.Property;

@Mod(version = Version.VERSION, modid = "BuildCraft|Transport", name = "Buildcraft Transport", dependencies = DefaultProps.DEPENDENCY_CORE)
@NetworkMod(channels = {DefaultProps.NET_CHANNEL_NAME}, packetHandler = PacketHandlerTransport.class)
public class BuildCraftTransport {

	public static BlockGenericPipe genericPipeBlock;
	public static float pipeDurability;
	public static Item pipeWaterproof;
	public static Item pipeGate;
	public static Item pipeGateAutarchic;
	public static Item redPipeWire;
	public static Item bluePipeWire;
	public static Item greenPipeWire;
	public static Item yellowPipeWire;
	public static Item pipeItemsWood;
	public static Item pipeItemsEmerald;
	public static Item pipeItemsStone;
	public static Item pipeItemsCobblestone;
	public static Item pipeItemsIron;
	public static Item pipeItemsQuartz;
	public static Item pipeItemsGold;
	public static Item pipeItemsDiamond;
	public static Item pipeItemsObsidian;
	public static Item pipeItemsLapis;
	public static Item pipeItemsDaizuli;
	public static Item pipeItemsVoid;
	public static Item pipeItemsSandstone;
	public static Item pipeFluidsWood;
	public static Item pipeFluidsCobblestone;
	public static Item pipeFluidsQuartz;
	public static Item pipeFluidsStone;
	public static Item pipeFluidsIron;
	public static Item pipeFluidsGold;
	public static Item pipeFluidsVoid;
	public static Item pipeFluidsSandstone;
	public static Item pipeFluidsEmerald;
	public static Item pipePowerWood;
	public static Item pipePowerCobblestone;
	public static Item pipePowerStone;
	public static Item pipePowerQuartz;
	public static Item pipePowerGold;
	public static Item pipePowerDiamond;
	public static ItemFacade facadeItem;
	public static Item plugItem;
	public static BlockFilteredBuffer filteredBufferBlock;
	// public static Item pipeItemsStipes;
	public static Item pipeStructureCobblestone;
	public static int groupItemsTrigger;
	public static BCTrigger triggerPipeEmpty = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_EMPTY, Kind.Empty);
	public static BCTrigger triggerPipeItems = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_ITEMS, Kind.ContainsItems);
	public static BCTrigger triggerPipeFluids = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_LIQUIDS, Kind.ContainsFluids);
	public static BCTrigger triggerPipeContainsEnergy = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_CONTAINS_ENERGY, Kind.ContainsEnergy);
	public static BCTrigger triggerPipeRequestsEnergy = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_REQUESTS_ENERGY, Kind.RequestsEnergy);
	public static BCTrigger triggerPipeTooMuchEnergy = new TriggerPipeContents(DefaultProps.TRIGGER_PIPE_TOO_MUCH_ENERGY, Kind.TooMuchEnergy);
	public static BCTrigger triggerRedSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_ACTIVE, true, IPipe.WireColor.Red);
	public static BCTrigger triggerRedSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_RED_SIGNAL_INACTIVE, false, IPipe.WireColor.Red);
	public static BCTrigger triggerBlueSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_ACTIVE, true, IPipe.WireColor.Blue);
	public static BCTrigger triggerBlueSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_BLUE_SIGNAL_INACTIVE, false, IPipe.WireColor.Blue);
	public static BCTrigger triggerGreenSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_ACTIVE, true, IPipe.WireColor.Green);
	public static BCTrigger triggerGreenSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_GREEN_SIGNAL_INACTIVE, false, IPipe.WireColor.Green);
	public static BCTrigger triggerYellowSignalActive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_ACTIVE, true, IPipe.WireColor.Yellow);
	public static BCTrigger triggerYellowSignalInactive = new TriggerPipeSignal(DefaultProps.TRIGGER_YELLOW_SIGNAL_INACTIVE, false, IPipe.WireColor.Yellow);
	public static BCTrigger triggerInventoryBelow25 = new TriggerFilteredBufferInventoryLevel(DefaultProps.TRIGGER_INVENTORY_LEVEL_BELOW25, TriggerFilteredBufferInventoryLevel.State.Below25);
	public static BCTrigger triggerInventoryBelow50 = new TriggerFilteredBufferInventoryLevel(DefaultProps.TRIGGER_INVENTORY_LEVEL_BELOW50, TriggerFilteredBufferInventoryLevel.State.Below50);
	public static BCTrigger triggerInventoryBelow75 = new TriggerFilteredBufferInventoryLevel(DefaultProps.TRIGGER_INVENTORY_LEVEL_BELOW75, TriggerFilteredBufferInventoryLevel.State.Below75);
	public static BCAction actionRedSignal = new ActionSignalOutput(DefaultProps.ACTION_RED_SIGNAL, IPipe.WireColor.Red);
	public static BCAction actionBlueSignal = new ActionSignalOutput(DefaultProps.ACTION_BLUE_SIGNAL, IPipe.WireColor.Blue);
	public static BCAction actionGreenSignal = new ActionSignalOutput(DefaultProps.ACTION_GREEN_SIGNAL, IPipe.WireColor.Green);
	public static BCAction actionYellowSignal = new ActionSignalOutput(DefaultProps.ACTION_YELLOW_SIGNAL, IPipe.WireColor.Yellow);
	public static BCAction actionEnergyPulser = new ActionEnergyPulser(DefaultProps.ACTION_ENERGY_PULSER);
	public static BCAction actionSingleEnergyPulse = new ActionSingleEnergyPulse(DefaultProps.ACTION_SINGLE_ENERGY_PULSE);
	public static BCAction[] actionPipeColor = new BCAction[16];
	public static BCAction[] actionPipeDirection = new BCAction[16];
	@Instance("BuildCraft|Transport")
	public static BuildCraftTransport instance;
	public IIconProvider pipeIconProvider = new PipeIconProvider();
	public IIconProvider gateIconProvider = new GateIconProvider();
	public IIconProvider wireIconProvider = new WireIconProvider();

	private static class PipeRecipe {

		boolean isShapeless = false; // pipe recipes come shaped and unshaped.
		ItemStack result;
		Object[] input;
	}

	private static class ExtractionHandler implements IExtractionHandler {

		private final String[] items;
		private final String[] liquids;

		public ExtractionHandler(String[] items, String[] liquids) {
			this.items = items;
			this.liquids = liquids;
		}

		@Override
		public boolean canExtractItems(Object extractor, World world, int i, int j, int k) {
			return testStrings(items, world, i, j, k);
		}

		@Override
		public boolean canExtractFluids(Object extractor, World world, int i, int j, int k) {
			return testStrings(liquids, world, i, j, k);
		}

		private boolean testStrings(String[] excludedBlocks, World world, int i, int j, int k) {
			int id = world.getBlockId(i, j, k);
			Block block = Block.blocksList[id];
			if (block == null)
				return false;

			int meta = world.getBlockMetadata(i, j, k);

			for (String excluded : excludedBlocks) {
				if (excluded.equals(block.getUnlocalizedName()))
					return false;

				String[] tokens = excluded.split(":");
				if (tokens[0].equals(Integer.toString(id)) && (tokens.length == 1 || tokens[1].equals(Integer.toString(meta))))
					return false;
			}
			return true;
		}
	}
	private static LinkedList<PipeRecipe> pipeRecipes = new LinkedList<PipeRecipe>();

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		try {
			Property durability = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.durability", DefaultProps.PIPES_DURABILITY);
			durability.comment = "How long a pipe will take to break";
			pipeDurability = (float) durability.getDouble(DefaultProps.PIPES_DURABILITY);

			Property exclusionItemList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "woodenPipe.item.exclusion", new String[0]);

			String[] excludedItemBlocks = exclusionItemList.getStringList();
			if (excludedItemBlocks != null) {
				for (int j = 0; j < excludedItemBlocks.length; ++j) {
					excludedItemBlocks[j] = excludedItemBlocks[j].trim();
				}
			} else
				excludedItemBlocks = new String[0];

			Property exclusionFluidList = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_BLOCK, "woodenPipe.liquid.exclusion", new String[0]);

			String[] excludedFluidBlocks = exclusionFluidList.getStringList();
			if (excludedFluidBlocks != null) {
				for (int j = 0; j < excludedFluidBlocks.length; ++j) {
					excludedFluidBlocks[j] = excludedFluidBlocks[j].trim();
				}
			} else
				excludedFluidBlocks = new String[0];

			PipeManager.registerExtractionHandler(new ExtractionHandler(excludedItemBlocks, excludedFluidBlocks));

			Property groupItemsTriggerProp = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_GENERAL, "pipes.groupItemsTrigger", 32);
			groupItemsTriggerProp.comment = "when reaching this amount of objects in a pipes, items will be automatically grouped";
			groupItemsTrigger = groupItemsTriggerProp.getInt();


			Property genericPipeId = BuildCraftCore.mainConfiguration.getBlock("pipe.id", DefaultProps.GENERIC_PIPE_ID);

			Property pipeWaterproofId = BuildCraftCore.mainConfiguration.getItem("pipeWaterproof.id", DefaultProps.PIPE_WATERPROOF_ID);

			pipeWaterproof = new ItemBuildCraft(pipeWaterproofId.getInt());
			pipeWaterproof.setUnlocalizedName("pipeWaterproof");
			pipeWaterproof.setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
			LanguageRegistry.addName(pipeWaterproof, "Pipe Waterproof");
			genericPipeBlock = new BlockGenericPipe(genericPipeId.getInt());
			CoreProxy.proxy.registerBlock(genericPipeBlock.setUnlocalizedName("pipeBlock"), ItemBlock.class);

			// Fixing retro-compatiblity
			pipeItemsWood = buildPipe(DefaultProps.PIPE_ITEMS_WOOD_ID, PipeItemsWood.class, "Wooden Transport Pipe", "plankWood", Block.glass, "plankWood");
			pipeItemsEmerald = buildPipe(DefaultProps.PIPE_ITEMS_EMERALD_ID, PipeItemsEmerald.class, "Emerald Transport Pipe", Item.emerald, Block.glass, Item.emerald);
			pipeItemsCobblestone = buildPipe(DefaultProps.PIPE_ITEMS_COBBLESTONE_ID, PipeItemsCobblestone.class, "Cobblestone Transport Pipe", Block.cobblestone, Block.glass, Block.cobblestone);
			pipeItemsStone = buildPipe(DefaultProps.PIPE_ITEMS_STONE_ID, PipeItemsStone.class, "Stone Transport Pipe", Block.stone, Block.glass, Block.stone);
			pipeItemsQuartz = buildPipe(DefaultProps.PIPE_ITEMS_QUARTZ_ID, PipeItemsQuartz.class, "Quartz Transport Pipe", Block.blockNetherQuartz, Block.glass, Block.blockNetherQuartz);
			pipeItemsIron = buildPipe(DefaultProps.PIPE_ITEMS_IRON_ID, PipeItemsIron.class, "Iron Transport Pipe", Item.ingotIron, Block.glass, Item.ingotIron);
			pipeItemsGold = buildPipe(DefaultProps.PIPE_ITEMS_GOLD_ID, PipeItemsGold.class, "Golden Transport Pipe", Item.ingotGold, Block.glass, Item.ingotGold);
			pipeItemsDiamond = buildPipe(DefaultProps.PIPE_ITEMS_DIAMOND_ID, PipeItemsDiamond.class, "Diamond Transport Pipe", Item.diamond, Block.glass, Item.diamond);
			pipeItemsObsidian = buildPipe(DefaultProps.PIPE_ITEMS_OBSIDIAN_ID, PipeItemsObsidian.class, "Obsidian Transport Pipe", Block.obsidian, Block.glass, Block.obsidian);
			pipeItemsLapis = buildPipe(DefaultProps.PIPE_ITEMS_LAPIS_ID, PipeItemsLapis.class, "Lapis Transport Pipe", Block.blockLapis, Block.glass, Block.blockLapis);
			pipeItemsDaizuli = buildPipe(DefaultProps.PIPE_ITEMS_DAIZULI_ID, PipeItemsDaizuli.class, "Daizuli Transport Pipe", Block.blockLapis, Block.glass, Item.diamond);
			pipeItemsSandstone = buildPipe(DefaultProps.PIPE_ITEMS_SANDSTONE_ID, PipeItemsSandstone.class, "Sandstone Transport Pipe", Block.sandStone, Block.glass, Block.sandStone);
			pipeItemsVoid = buildPipe(DefaultProps.PIPE_ITEMS_VOID_ID, PipeItemsVoid.class, "Void Transport Pipe", "dyeBlack", Block.glass, Item.redstone);

			pipeFluidsWood = buildPipe(DefaultProps.PIPE_LIQUIDS_WOOD_ID, PipeFluidsWood.class, "Wooden Waterproof Pipe", pipeWaterproof, pipeItemsWood);
			pipeFluidsCobblestone = buildPipe(DefaultProps.PIPE_LIQUIDS_COBBLESTONE_ID, PipeFluidsCobblestone.class, "Cobblestone Waterproof Pipe", pipeWaterproof, pipeItemsCobblestone);
			pipeFluidsStone = buildPipe(DefaultProps.PIPE_LIQUIDS_STONE_ID, PipeFluidsStone.class, "Stone Waterproof Pipe", pipeWaterproof, pipeItemsStone);
			pipeFluidsIron = buildPipe(DefaultProps.PIPE_LIQUIDS_IRON_ID, PipeFluidsIron.class, "Iron Waterproof Pipe", pipeWaterproof, pipeItemsIron);
			pipeFluidsGold = buildPipe(DefaultProps.PIPE_LIQUIDS_GOLD_ID, PipeFluidsGold.class, "Golden Waterproof Pipe", pipeWaterproof, pipeItemsGold);
			pipeFluidsEmerald = buildPipe(DefaultProps.PIPE_LIQUIDS_EMERALD_ID, PipeFluidsEmerald.class, "Emerald Waterproof Pipe", pipeWaterproof, pipeItemsEmerald);
			pipeFluidsSandstone = buildPipe(DefaultProps.PIPE_LIQUIDS_SANDSTONE_ID, PipeFluidsSandstone.class, "Sandstone Waterproof Pipe", pipeWaterproof, pipeItemsSandstone);
			pipeFluidsVoid = buildPipe(DefaultProps.PIPE_LIQUIDS_VOID_ID, PipeFluidsVoid.class, "Void Waterproof Pipe", pipeWaterproof, pipeItemsVoid);
			pipeFluidsQuartz = buildPipe(DefaultProps.PIPE_LIQUIDS_QUARTZ_ID, PipeFluidsQuartz.class, "Quartz Waterproof Pipe", pipeWaterproof, pipeItemsQuartz);

			pipePowerWood = buildPipe(DefaultProps.PIPE_POWER_WOOD_ID, PipePowerWood.class, "Wooden Conductive Pipe", Item.redstone, pipeItemsWood);
			pipePowerCobblestone = buildPipe(DefaultProps.PIPE_POWER_COBBLESTONE_ID, PipePowerCobblestone.class, "Cobblestone Conductive Pipe", Item.redstone, pipeItemsCobblestone);
			pipePowerStone = buildPipe(DefaultProps.PIPE_POWER_STONE_ID, PipePowerStone.class, "Stone Conductive Pipe", Item.redstone, pipeItemsStone);
			pipePowerQuartz = buildPipe(DefaultProps.PIPE_POWER_QUARTZ_ID, PipePowerQuartz.class, "Quartz Conductive Pipe", Item.redstone, pipeItemsQuartz);
			pipePowerGold = buildPipe(DefaultProps.PIPE_POWER_GOLD_ID, PipePowerGold.class, "Golden Conductive Pipe", Item.redstone, pipeItemsGold);
			pipePowerDiamond = buildPipe(DefaultProps.PIPE_POWER_DIAMOND_ID, PipePowerDiamond.class, "Diamond Conductive Pipe", Item.redstone, pipeItemsDiamond);

			pipeStructureCobblestone = buildPipe(DefaultProps.PIPE_STRUCTURE_COBBLESTONE_ID, PipeStructureCobblestone.class, "Cobblestone Structure Pipe", Block.gravel, pipeItemsCobblestone);

			// Fix the recipe
			// pipeItemsStipes = createPipe(DefaultProps.PIPE_ITEMS_STRIPES_ID, PipeItemsStripes.class, "Stripes Transport Pipe", new ItemStack(Item.dyePowder,
			// 1, 0), Block.glass, new ItemStack(Item.dyePowder, 1, 11));

			Property redPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "redPipeWire.id", DefaultProps.RED_PIPE_WIRE);
			redPipeWire = new ItemBuildCraft(redPipeWireId.getInt());
			redPipeWire.setUnlocalizedName("redPipeWire");
			LanguageRegistry.addName(redPipeWire, "Red Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.dyePowder, 1, 1), new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1)}, 500, new ItemStack(redPipeWire, 8)));

			Property bluePipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "bluePipeWire.id", DefaultProps.BLUE_PIPE_WIRE);
			bluePipeWire = new ItemBuildCraft(bluePipeWireId.getInt());
			bluePipeWire.setUnlocalizedName("bluePipeWire");
			LanguageRegistry.addName(bluePipeWire, "Blue Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.dyePowder, 1, 4), new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1)}, 500, new ItemStack(bluePipeWire, 8)));

			Property greenPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "greenPipeWire.id", DefaultProps.GREEN_PIPE_WIRE);
			greenPipeWire = new ItemBuildCraft(greenPipeWireId.getInt());
			greenPipeWire.setUnlocalizedName("greenPipeWire");
			LanguageRegistry.addName(greenPipeWire, "Green Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.dyePowder, 1, 2), new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1)}, 500, new ItemStack(greenPipeWire, 8)));

			Property yellowPipeWireId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "yellowPipeWire.id", DefaultProps.YELLOW_PIPE_WIRE);
			yellowPipeWire = new ItemBuildCraft(yellowPipeWireId.getInt());
			yellowPipeWire.setUnlocalizedName("yellowPipeWire");
			LanguageRegistry.addName(yellowPipeWire, "Yellow Pipe Wire");
			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(Item.dyePowder, 1, 11), new ItemStack(Item.redstone, 1),
				new ItemStack(Item.ingotIron, 1)}, 500, new ItemStack(yellowPipeWire, 8)));

			Property pipeGateId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeGate.id", DefaultProps.GATE_ID);
			pipeGate = new ItemGate(pipeGateId.getInt(), 0);
			pipeGate.setUnlocalizedName("pipeGate");

			Property pipeGateAutarchicId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeGateAutarchic.id",
					DefaultProps.GATE_AUTARCHIC_ID);
			pipeGateAutarchic = new ItemGate(pipeGateAutarchicId.getInt(), 1);
			pipeGateAutarchic.setUnlocalizedName("pipeGateAutarchic");

			Property pipeFacadeId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipeFacade.id", DefaultProps.PIPE_FACADE_ID);
			facadeItem = new ItemFacade(pipeFacadeId.getInt());
			facadeItem.setUnlocalizedName("pipeFacade");

			Property pipePlugId = BuildCraftCore.mainConfiguration.get(Configuration.CATEGORY_ITEM, "pipePlug.id", DefaultProps.PIPE_PLUG_ID);
			plugItem = new ItemPlug(pipePlugId.getInt());
			plugItem.setUnlocalizedName("pipePlug");

			Property filteredBufferId = BuildCraftCore.mainConfiguration.getBlock("filteredBuffer.id", DefaultProps.FILTERED_BUFFER_ID);
			filteredBufferBlock = new BlockFilteredBuffer(filteredBufferId.getInt());
			CoreProxy.proxy.registerBlock(filteredBufferBlock.setUnlocalizedName("filteredBufferBlock"));
			CoreProxy.proxy.addName(filteredBufferBlock, "Filtered Buffer");

			AssemblyRecipe.assemblyRecipes.add(new AssemblyRecipe(new ItemStack[]{new ItemStack(pipeStructureCobblestone)}, 1000, new ItemStack(plugItem, 8)));

		} finally {
			BuildCraftCore.mainConfiguration.save();
		}
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		// Register connection handler
		// MinecraftForge.registerConnectionHandler(new ConnectionHandler());

		// Register gui handler
		// MinecraftForge.setGuiHandler(mod_BuildCraftTransport.instance, new GuiHandler());

		TransportProxy.proxy.registerTileEntities();

		// dockingStationBlock = new
		// BlockDockingStation(Integer.parseInt(dockingStationId.value));
		// ModLoader.registerBlock(dockingStationBlock);
		// CoreProxy.addName(dockingStationBlock.setBlockName("dockingStation"),
		// "Docking Station");

		// ModLoader.RegisterTileEntity(TileDockingStation.class,
		// "net.minecraft.src.buildcraft.TileDockingStation");

		new BptBlockPipe(genericPipeBlock.blockID);

		BuildCraftCore.itemBptProps[pipeItemsWood.itemID] = new BptItemPipeWooden();
		BuildCraftCore.itemBptProps[pipeFluidsWood.itemID] = new BptItemPipeWooden();
		BuildCraftCore.itemBptProps[pipeItemsIron.itemID] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeFluidsIron.itemID] = new BptItemPipeIron();
		BuildCraftCore.itemBptProps[pipeItemsDiamond.itemID] = new BptItemPipeDiamond();
		BuildCraftCore.itemBptProps[pipeItemsEmerald.itemID] = new BptItemPipeEmerald();

		ActionManager.registerTriggerProvider(new PipeTriggerProvider());

		if (BuildCraftCore.loadDefaultRecipes) {
			loadRecipes();
		}

		TransportProxy.proxy.registerRenderers();
		NetworkRegistry.instance().registerGuiHandler(instance, new GuiHandler());
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		ItemFacade.initialize();

		for (EnumColor color : EnumColor.VALUES) {
			actionPipeColor[color.ordinal()] = new ActionPipeColor(-1, color);
		}

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
			actionPipeDirection[direction.ordinal()] = new ActionPipeDirection(-1, direction);
		}
	}

	public void loadRecipes() {

		// Add base recipe for pipe waterproof.
		GameRegistry.addShapelessRecipe(new ItemStack(pipeWaterproof, 1), new ItemStack(Item.dyePowder, 1, 2));

		// Add pipe recipes
		for (PipeRecipe pipe : pipeRecipes) {
			if (pipe.isShapeless) {
				GameRegistry.addShapelessRecipe(pipe.result, pipe.input);
			} else {
				CoreProxy.proxy.addCraftingRecipe(pipe.result, pipe.input);
			}
		}

		CoreProxy.proxy.addCraftingRecipe(new ItemStack(filteredBufferBlock, 1),
				new Object[]{"wdw", "wcw", "wpw", Character.valueOf('w'), "plankWood", Character.valueOf('d'),
			BuildCraftTransport.pipeItemsDiamond, Character.valueOf('c'), Block.chest, Character.valueOf('p'),
			Block.pistonBase});

		//Facade turning helper
		GameRegistry.addRecipe(facadeItem.new FacadeRecipe());
	}

	@EventHandler
	public void processIMCRequests(FMLInterModComms.IMCEvent event) {
		Splitter splitter = Splitter.on("@").trimResults();
		for (IMCMessage m : event.getMessages()) {
			if ("add-facade".equals(m.key)) {
				String[] array = Iterables.toArray(splitter.split(m.getStringValue()), String.class);
				if (array.length != 2) {
					Logger.getLogger("Buildcraft").log(Level.INFO,
							String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
					continue;
				}
				Integer blId = Ints.tryParse(array[0]);
				Integer metaId = Ints.tryParse(array[1]);
				if (blId == null || metaId == null) {
					Logger.getLogger("Buildcraft").log(Level.INFO,
							String.format("Received an invalid add-facade request %s from mod %s", m.getStringValue(), m.getSender()));
					continue;
				}
				ItemFacade.addFacade(new ItemStack(blId, 1, metaId));
			}
		}
	}

	public static Item buildPipe(int defaultID, Class<? extends Pipe> clas, String descr, Object... ingredients) {
		String name = Character.toLowerCase(clas.getSimpleName().charAt(0)) + clas.getSimpleName().substring(1);

		Property prop = BuildCraftCore.mainConfiguration.getItem(name + ".id", defaultID);

		int id = prop.getInt(defaultID);
		ItemPipe res = BlockGenericPipe.registerPipe(id, clas);
		res.setUnlocalizedName(clas.getSimpleName());
		LanguageRegistry.addName(res, descr);

		// Add appropriate recipe to temporary list
		PipeRecipe recipe = new PipeRecipe();

		if (ingredients.length == 3) {
			recipe.result = new ItemStack(res, 8);
			recipe.input = new Object[]{"ABC", Character.valueOf('A'), ingredients[0], Character.valueOf('B'), ingredients[1], Character.valueOf('C'), ingredients[2]};

			pipeRecipes.add(recipe);
		} else if (ingredients.length == 2) {
			recipe.isShapeless = true;
			recipe.result = new ItemStack(res, 1);
			recipe.input = new Object[]{ingredients[0], ingredients[1]};

			pipeRecipes.add(recipe);

			if (ingredients[1] instanceof ItemPipe) {
				PipeRecipe uncraft = new PipeRecipe();
				uncraft.isShapeless = true;
				uncraft.input = new Object[]{new ItemStack(res)};
				uncraft.result = new ItemStack((Item) ingredients[1]);
				pipeRecipes.add(uncraft);
			}
		}

		return res;
	}
}
