package buildcraft.core.village;

import static net.minecraftforge.common.ChestGenHooks.VILLAGE_BLACKSMITH;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces.Start;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

import net.minecraftforge.common.ChestGenHooks;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftFactory;

public class ComponentBuildcraftHouse extends StructureVillagePieces.House2 {

	private int averageGroundLevel = -1;

	public ComponentBuildcraftHouse() {
	}

	public ComponentBuildcraftHouse(Start startPiece, int par2, Random par3Random, StructureBoundingBox par4StructureBoundingBox, int par5) {
		super();
		this.coordBaseMode = par5;
		this.boundingBox = par4StructureBoundingBox;

	}

	public static ComponentBuildcraftHouse buildComponent(Start startPiece, List pieces, Random random, int p1, int p2, int p3, int p4, int p5) {
		StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(p1, p2, p3, 0, -1, 0, 12, 4, 12, p4);
		return canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null ? new ComponentBuildcraftHouse(startPiece, p5, random, structureboundingbox, p4) : null;
	}

	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox sbb) {
	    if (this.averageGroundLevel < 0)
            {
                this.averageGroundLevel = this.getAverageGroundLevel(world, sbb);

                if (this.averageGroundLevel < 0)
                {
                    return true;
                }

                this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.maxY + 3, 0);
            }

            this.fillWithBlocks(world, sbb, 0, 1, 0, 9, 4, 6, Blocks.air, Blocks.air, false);
            this.fillWithBlocks(world, sbb, 0, 0, 0, 9, 0, 6, Blocks.brick_block, Blocks.brick_block, false);
            this.fillWithBlocks(world, sbb, 0, 4, 0, 9, 4, 6, Blocks.brick_block, Blocks.brick_block, false);
            this.fillWithBlocks(world, sbb, 0, 5, 0, 9, 5, 6, Blocks.stone_slab, Blocks.stone_slab, false);
            this.fillWithBlocks(world, sbb, 1, 5, 1, 8, 5, 5, Blocks.air, Blocks.air, false);
            this.fillWithBlocks(world, sbb, 1, 1, 0, 2, 3, 0, Blocks.planks, Blocks.planks, false);
            this.fillWithBlocks(world, sbb, 0, 1, 0, 0, 4, 0, Blocks.log, Blocks.log, false);
            this.fillWithBlocks(world, sbb, 3, 1, 0, 3, 4, 0, Blocks.log, Blocks.log, false);
            this.fillWithBlocks(world, sbb, 0, 1, 6, 0, 4, 6, Blocks.log, Blocks.log, false);
            this.placeBlockAtCurrentPosition(world, Blocks.planks, 0, 3, 3, 1, sbb);
            this.fillWithBlocks(world, sbb, 3, 1, 2, 3, 3, 2, Blocks.planks, Blocks.planks, false);
            this.fillWithBlocks(world, sbb, 4, 1, 3, 5, 3, 3, Blocks.planks, Blocks.planks, false);
            this.fillWithBlocks(world, sbb, 0, 1, 1, 0, 3, 5, Blocks.planks, Blocks.planks, false);
            this.fillWithBlocks(world, sbb, 1, 1, 6, 5, 3, 6, Blocks.planks, Blocks.planks, false);
            this.fillWithBlocks(world, sbb, 5, 1, 0, 5, 3, 0, Blocks.fence, Blocks.fence, false);
            this.fillWithBlocks(world, sbb, 9, 1, 0, 9, 3, 0, Blocks.fence, Blocks.fence, false);
            this.fillWithBlocks(world, sbb, 6, 1, 4, 9, 4, 6, Blocks.brick_block, Blocks.brick_block, false);
            this.placeBlockAtCurrentPosition(world, BuildCraftEnergy.blockFuel, 0, 7, 1, 5, sbb);
            this.placeBlockAtCurrentPosition(world, BuildCraftEnergy.blockFuel, 0, 8, 1, 5, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 9, 2, 5, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.iron_bars, 0, 9, 2, 4, sbb);
            this.fillWithBlocks(world, sbb, 7, 2, 4, 8, 2, 5, Blocks.air, Blocks.air, false);
            this.placeBlockAtCurrentPosition(world, Blocks.brick_block, 0, 6, 1, 3, sbb);
            this.placeBlockAtCurrentPosition(world, BuildCraftFactory.tankBlock, 0, 6, 2, 3, sbb);
            this.placeBlockAtCurrentPosition(world, BuildCraftFactory.tankBlock, 0, 6, 3, 3, sbb);
            this.placeBlockAtCurrentPosition(world, BuildCraftFactory.autoWorkbenchBlock, 0, 8, 1, 1, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 2, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 0, 2, 4, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 2, 2, 6, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, 4, 2, 6, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.fence, 0, 2, 1, 4, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.wooden_pressure_plate, 0, 2, 2, 4, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.planks, 0, 1, 1, 5, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, this.getMetadataWithOffset(Blocks.oak_stairs, 3), 2, 1, 5, sbb);
            this.placeBlockAtCurrentPosition(world, Blocks.oak_stairs, this.getMetadataWithOffset(Blocks.oak_stairs, 1), 1, 1, 4, sbb);
            int i;
            int j;

            for (i = 6; i <= 8; ++i)
            {
                if (this.getBlockAtCurrentPosition(world, i, 0, -1, sbb).getMaterial() == Material.air && this.getBlockAtCurrentPosition(world, i, -1, -1, sbb).getMaterial() != Material.air)
                {
                    this.placeBlockAtCurrentPosition(world, Blocks.stone_stairs, this.getMetadataWithOffset(Blocks.stone_stairs, 3), i, 0, -1, sbb);
                }
            }

            for (i = 0; i < 7; ++i)
            {
                for (j = 0; j < 10; ++j)
                {
                    this.clearCurrentPositionBlocksUpwards(world, j, 6, i, sbb);
                    this.func_151554_b(world, Blocks.brick_block, 0, j, -1, i, sbb);
                }
            }

            this.spawnVillagers(world, sbb, 7, 1, 1, 1);
            return true;
        }

	@Override
	protected int getVillagerType(int par1) {
		return BuildCraftCore.bcVillagerID;
	}
}
