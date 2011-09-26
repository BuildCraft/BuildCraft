package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.DefaultProps;
import net.minecraft.src.buildcraft.factory.BlockAutoWorkbench;
import net.minecraft.src.buildcraft.factory.BlockFrame;
import net.minecraft.src.buildcraft.factory.BlockMiningWell;
import net.minecraft.src.buildcraft.factory.BlockPlainPipe;
import net.minecraft.src.buildcraft.factory.BlockPump;
import net.minecraft.src.buildcraft.factory.BlockQuarry;
import net.minecraft.src.buildcraft.factory.BlockRefinery;
import net.minecraft.src.buildcraft.factory.BlockTank;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileMiningWell;
import net.minecraft.src.buildcraft.factory.TilePump;
import net.minecraft.src.buildcraft.factory.TileQuarry;
import net.minecraft.src.buildcraft.factory.TileTank;
import net.minecraft.src.forge.Configuration;
import net.minecraft.src.forge.Property;

public class BuildCraftFactory {
	public static BlockQuarry quarryBlock;	
	public static BlockMiningWell miningWellBlock;
	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockFrame frameBlock;	
	public static BlockPlainPipe plainPipeBlock;
	public static BlockPump pumpBlock;
	public static BlockTank tankBlock;
	public static BlockRefinery refineryBlock;
	
	public static int drillTexture;
	
	public static void initialize () {		
		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		boolean allowMining = Boolean
				.parseBoolean(BuildCraftCore.mainConfiguration
						.getOrCreateBooleanProperty("mining.enabled",
								Configuration.GENERAL_PROPERTY, true).value);
		
		Property minigWellId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("miningWell.id",
						DefaultProps.MINING_WELL_ID);
		Property plainPipeId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("drill.id", DefaultProps.DRILL_ID);
		Property autoWorkbenchId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("autoWorkbench.id",
						DefaultProps.AUTO_WORKBENCH_ID);
		Property frameId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("frame.id", DefaultProps.FRAME_ID);
		Property quarryId = BuildCraftCore.mainConfiguration
				.getOrCreateBlockIdProperty("quarry.id", DefaultProps.QUARRY_ID);
		Property pumpId = BuildCraftCore.mainConfiguration
		   .getOrCreateBlockIdProperty("pump.id", DefaultProps.PUMP_ID);
		Property tankId = BuildCraftCore.mainConfiguration
		   .getOrCreateBlockIdProperty("tank.id", DefaultProps.TANK_ID);
		
		BuildCraftCore.mainConfiguration.save();
		
		miningWellBlock = new BlockMiningWell(Integer.parseInt(minigWellId.value));
		ModLoader.RegisterBlock(miningWellBlock);
		CoreProxy.addName(miningWellBlock.setBlockName("miningWellBlock"), "Mining Well");
		
		if (allowMining) {
			craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
				"ipi", "igi", "iPi", Character.valueOf('p'), Item.redstone,
				Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
				BuildCraftCore.ironGearItem, Character.valueOf('P'),
				Item.pickaxeSteel });
		}
		
		plainPipeBlock = new BlockPlainPipe(Integer.parseInt(plainPipeId.value));
		ModLoader.RegisterBlock(plainPipeBlock);
		CoreProxy.addName(plainPipeBlock.setBlockName("plainPipeBlock"), "Mining Pipe");
		
		autoWorkbenchBlock = new BlockAutoWorkbench(
				Integer.parseInt(autoWorkbenchId.value));
		ModLoader.RegisterBlock(autoWorkbenchBlock);
		craftingmanager.addRecipe(
				new ItemStack(autoWorkbenchBlock),
				new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
						Block.workbench, Character.valueOf('g'),
						BuildCraftCore.woodenGearItem });
		CoreProxy.addName(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"),
				"Automatic Crafting Table");
				
		frameBlock = new BlockFrame(Integer.parseInt(frameId.value));
		ModLoader.RegisterBlock(frameBlock);
		CoreProxy.addName(frameBlock.setBlockName("frameBlock"), "Frame");
		
		quarryBlock = new BlockQuarry(Integer.parseInt(quarryId.value));
		ModLoader.RegisterBlock(quarryBlock);			
		
		if (allowMining) {
			craftingmanager.addRecipe(
					new ItemStack(quarryBlock),
					new Object[] { "ipi", "gig", "dDd", 
						Character.valueOf('i'), BuildCraftCore.ironGearItem,
						Character.valueOf('p'), Item.redstone,
						Character.valueOf('g'),	BuildCraftCore.goldGearItem,
						Character.valueOf('d'),	BuildCraftCore.diamondGearItem,
						Character.valueOf('D'),	Item.pickaxeDiamond,
					});
		}
		
		CoreProxy.addName(quarryBlock.setBlockName("machineBlock"),
		"Quarry");
		
		tankBlock = new BlockTank(Integer.parseInt(tankId.value));
		craftingmanager.addRecipe(
				new ItemStack(tankBlock),
				new Object[] { "ggg", "g g", "ggg", 
					Character.valueOf('g'), Block.glass,
				});
		CoreProxy.addName(tankBlock.setBlockName("tankBlock"),
		"Tank");
		ModLoader.RegisterBlock(tankBlock);			
		
		pumpBlock = new BlockPump(Integer.parseInt(pumpId.value));
		craftingmanager.addRecipe(
				new ItemStack(pumpBlock),
				new Object[] { "T ", "W ", 
					Character.valueOf('T'), tankBlock,
					Character.valueOf('W'), miningWellBlock,
				});
		CoreProxy.addName(pumpBlock.setBlockName("pumpBlock"),
		"Pump");
		ModLoader.RegisterBlock(pumpBlock);	
		
		refineryBlock = new BlockRefinery(DefaultProps.REFINERY_ID);
		craftingmanager.addRecipe(
				new ItemStack(refineryBlock),
				new Object[] { "   ", "RTR", "TGT",
					Character.valueOf('T'), tankBlock,
					Character.valueOf('G'), BuildCraftCore.diamondGearItem,
					Character.valueOf('R'), Block.torchRedstoneActive,
				});
		CoreProxy.addName(refineryBlock.setBlockName("refineryBlock"),
		"Refinery");
		ModLoader.RegisterBlock(refineryBlock);
		
		ModLoader.RegisterTileEntity(TileQuarry.class, "Machine");		
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");
		ModLoader.RegisterTileEntity(TileAutoWorkbench.class, "AutoWorkbench");
		ModLoader.RegisterTileEntity(TilePump.class,
				"net.minecraft.src.buildcraft.factory.TilePump");
		ModLoader.RegisterTileEntity(TileTank.class,
		"net.minecraft.src.buildcraft.factory.TileTank");

		drillTexture = 2 * 16 + 1;
		
		BuildCraftCore.mainConfiguration.save();	
	}
}
