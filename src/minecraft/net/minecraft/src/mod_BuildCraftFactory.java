package net.minecraft.src;

import java.util.Map;

import net.minecraft.src.buildcraft.core.Core;
import net.minecraft.src.buildcraft.core.RenderVoid;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.factory.BlockAutoWorkbench;
import net.minecraft.src.buildcraft.factory.BlockFrame;
import net.minecraft.src.buildcraft.factory.BlockMachine;
import net.minecraft.src.buildcraft.factory.BlockMiningWell;
import net.minecraft.src.buildcraft.factory.BlockPlainPipe;
import net.minecraft.src.buildcraft.factory.EntityMechanicalArm;
import net.minecraft.src.buildcraft.factory.TileAutoWorkbench;
import net.minecraft.src.buildcraft.factory.TileMachine;
import net.minecraft.src.buildcraft.factory.TileMiningWell;

public class mod_BuildCraftFactory extends BaseMod {	
	
	public static BlockMachine machineBlock;
	
	public static BlockMiningWell miningWellBlock;

	public static BlockAutoWorkbench autoWorkbenchBlock;
	public static BlockFrame frameBlock;
	
	public static BlockPlainPipe plainPipeBlock;
	
	public static int drillTexture;
	
	public mod_BuildCraftFactory () {		
		mod_BuildCraftCore.initialize();
		mod_BuildCraftTransport.initialize();
		
		CraftingManager craftingmanager = CraftingManager.getInstance();
		
		miningWellBlock = new BlockMiningWell (Utils.getFirstFreeBlock ());
		ModLoader.RegisterBlock(miningWellBlock);
		ModLoader.AddName(miningWellBlock.setBlockName("miningWellBlock"), "Mining Well");
		craftingmanager.addRecipe(new ItemStack(miningWellBlock, 1), new Object[] {
			"ipi", "igi", "iPi", Character.valueOf('p'), mod_BuildCraftTransport.ironPipeBlock,
			Character.valueOf('i'), Item.ingotIron, Character.valueOf('g'),
			 mod_BuildCraftCore.ironGearItem, Character.valueOf('P'),
			Item.pickaxeSteel });	
		
		plainPipeBlock = new BlockPlainPipe (Utils.getFirstFreeBlock ());
		ModLoader.RegisterBlock(plainPipeBlock);
		ModLoader.AddName(plainPipeBlock.setBlockName("plainPipeBlock"), "Mining Pipe");
		
		autoWorkbenchBlock = new BlockAutoWorkbench (Utils.getFirstFreeBlock());
		ModLoader.RegisterBlock(autoWorkbenchBlock);
		craftingmanager.addRecipe(
				new ItemStack(autoWorkbenchBlock),
				new Object[] { " g ", "gwg", " g ", Character.valueOf('w'),
						Block.workbench, Character.valueOf('g'),
						mod_BuildCraftCore.woodenGearItem });
		ModLoader.AddName(autoWorkbenchBlock.setBlockName("autoWorkbenchBlock"),
				"Automatic Crafting Table");
				
		frameBlock = new BlockFrame (Utils.getFirstFreeBlock ());
		ModLoader.RegisterBlock(frameBlock);
		
		machineBlock = new BlockMachine (Utils.getFirstFreeBlock ());
		ModLoader.RegisterBlock(machineBlock);
		craftingmanager.addRecipe(
				new ItemStack(machineBlock),
				new Object[] { "ipi", "gdg", "dDd", 
					Character.valueOf('i'), mod_BuildCraftCore.ironGearItem,
					Character.valueOf('p'),	mod_BuildCraftTransport.diamondPipeBlock,
					Character.valueOf('g'),	mod_BuildCraftCore.goldGearItem,
					Character.valueOf('d'),	mod_BuildCraftCore.diamondGearItem,
					Character.valueOf('D'),	Item.pickaxeDiamond,
					});
		ModLoader.AddName(machineBlock.setBlockName("machineBlock"),
		"Quarry");
		
		ModLoader.RegisterTileEntity(TileMachine.class, "Machine");		
		ModLoader.RegisterTileEntity(TileMiningWell.class, "MiningWell");
		ModLoader.RegisterTileEntity(TileAutoWorkbench.class, "AutoWorkbench");

		drillTexture = ModLoader.addOverride("/terrain.png",
			"/net/minecraft/src/buildcraft/factory/gui/drill.png");

	}
		
	@Override
	public String Version() {
		return Core.version;
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public void AddRenderer(Map map) {
    	map.put (EntityMechanicalArm.class, new RenderVoid());
    }
	    
}
