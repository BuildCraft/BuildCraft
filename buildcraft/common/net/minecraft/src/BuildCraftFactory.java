package net.minecraft.src;

import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.BlockAutoWorkbench;
import net.minecraft.src.buildcraft.factory.BlockFrame;
import net.minecraft.src.buildcraft.factory.BlockMiningWell;
import net.minecraft.src.buildcraft.factory.BlockPlainPipe;
import net.minecraft.src.buildcraft.factory.BlockQuarry;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileMiningWell;
import net.minecraft.src.buildcraft.factory.TileQuarry;

public class BuildCraftFactory {
	public static BlockQuarry machineBlock;
	
	public static BlockMiningWell miningWellBlock;

	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockFrame frameBlock;
	
	public static BlockPlainPipe plainPipeBlock;
	
	public static int drillTexture;

	public static int tileQuarryDescriptionPacket = 120;

	public static final int craftingGUI = 123;
	
	public static void initialize () {		
		
		mod_BuildCraftCore.initialize();
		BuildCraftCore.initializeGears();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		miningWellBlock = new BlockMiningWell(Utils.getSafeBlockId(
				"miningWell.blockId", 150));
		ModLoader.RegisterBlock(miningWellBlock);
		CoreProxy.addName(miningWellBlock.setBlockName("miningWellBlock"), "Mining Well");
		craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
			"ipi", "igi", "iPi", Character.valueOf('p'), Item.redstone,
			Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
			 BuildCraftCore.ironGearItem, Character.valueOf('P'),
			Item.pickaxeSteel });	
		
		plainPipeBlock = new BlockPlainPipe(Utils.getSafeBlockId(
				"drill.blockId", 151));
		ModLoader.RegisterBlock(plainPipeBlock);
		CoreProxy.addName(plainPipeBlock.setBlockName("plainPipeBlock"), "Mining Pipe");
		
		autoWorkbenchBlock = new BlockAutoWorkbench(Utils.getSafeBlockId(
				"autoWorkbench.blockId", 152));
		ModLoader.RegisterBlock(autoWorkbenchBlock);
		craftingmanager.addRecipe(
				new ItemStack(autoWorkbenchBlock),
				new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
						Block.workbench, Character.valueOf('g'),
						BuildCraftCore.woodenGearItem });
		CoreProxy.addName(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"),
				"Automatic Crafting Table");
				
		frameBlock = new BlockFrame(Utils.getSafeBlockId("frame.blockId", 152));
		ModLoader.RegisterBlock(frameBlock);
		
		machineBlock = new BlockQuarry(Utils.getSafeBlockId("quarry.blockId",
				153));
		ModLoader.RegisterBlock(machineBlock);
		craftingmanager.addRecipe(
				new ItemStack(machineBlock),
				new Object[] { "ipi", "gig", "dDd", 
					Character.valueOf('i'), BuildCraftCore.ironGearItem,
					Character.valueOf('p'), Item.redstone,
					Character.valueOf('g'),	BuildCraftCore.goldGearItem,
					Character.valueOf('d'),	BuildCraftCore.diamondGearItem,
					Character.valueOf('D'),	Item.pickaxeDiamond,
					});
		CoreProxy.addName(machineBlock.setBlockName("machineBlock"),
		"Quarry");
		
		ModLoader.RegisterTileEntity(TileQuarry.class, "Machine");		
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");
		ModLoader.RegisterTileEntity(TileAutoWorkbench.class, "AutoWorkbench");

		drillTexture = 2 * 16 + 1;
		
		Utils.saveProperties();	
	}
}
