/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.Translation;
import buildcraft.builders.ItemBlueprint;
import buildcraft.builders.ItemBlueprintStandard;
import buildcraft.builders.ItemBlueprintTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.BlockScanner;

public class RecursiveBlueprintReader {

	private static final int SCANNER_ITERATION = 100;

	public TileArchitect architect;

	private BlockScanner blockScanner;
	private BlueprintBase writingBlueprint;
	private BptContext writingContext;

	private int subIndex = 0;
	private RecursiveBlueprintReader currentSubReader;
	private float computingTime = 0;

	private boolean done = false;
	private boolean saveInItem = false;

	private BlueprintBase parentBlueprint;

	public RecursiveBlueprintReader(TileArchitect iArchitect) {
		architect = iArchitect;
		ItemStack stack = architect.getStackInSlot(0);

		if (stack != null && stack.getItem() instanceof ItemBlueprint && architect.box.isInitialized()) {
			blockScanner = new BlockScanner(architect.box, architect.getWorld(), SCANNER_ITERATION);

			if (stack.getItem() instanceof ItemBlueprintStandard) {
				writingBlueprint = new Blueprint(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			} else if (stack.getItem() instanceof ItemBlueprintTemplate) {
				writingBlueprint = new Template(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			}

			writingContext = writingBlueprint.getContext(architect.getWorld(), architect.box);
			writingContext.readConfiguration = architect.readConfiguration;

			writingBlueprint.id.name = architect.name;
			writingBlueprint.author = architect.currentAuthorName;
			writingBlueprint.anchorX = architect.getPos().getX() - architect.box.xMin;
			writingBlueprint.anchorY = architect.getPos().getY() - architect.box.yMin;
			writingBlueprint.anchorZ = architect.getPos().getZ() - architect.box.zMin;
		} else {
			done = true;
		}
	}

	protected RecursiveBlueprintReader(TileArchitect iArchitect, BlueprintBase iParentBlueprint) {
		parentBlueprint = iParentBlueprint;
		architect = iArchitect;

		if (architect.box.isInitialized()) {
			blockScanner = new BlockScanner(architect.box, architect.getWorld(), SCANNER_ITERATION);

			if (parentBlueprint instanceof Blueprint) {
				writingBlueprint = new Blueprint(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			} else if (parentBlueprint instanceof Template) {
				writingBlueprint = new Template(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			}

			writingContext = writingBlueprint.getContext(architect.getWorld(), architect.box);
			writingContext.readConfiguration = architect.readConfiguration;

			writingBlueprint.id.name = architect.name;
			writingBlueprint.author = architect.currentAuthorName;
			writingBlueprint.anchorX = architect.getPos().getX() - architect.box.xMin;
			writingBlueprint.anchorY = architect.getPos().getY() - architect.box.yMin;
			writingBlueprint.anchorZ = architect.getPos().getZ() - architect.box.zMin;
		}
	}

	public void iterate() {
		if (done) {
			return;
		} else if (currentSubReader == null && subIndex < architect.subBlueprints.size()) {
			BlockPos subBlock = architect.subBlueprints.get(subIndex);

			TileEntity subTile = architect.getWorld().getTileEntity(subBlock);

			if (subTile instanceof TileArchitect) {
				TileArchitect subArchitect = (TileArchitect) subTile;
				currentSubReader = new RecursiveBlueprintReader(subArchitect, writingBlueprint);
			} else if (subTile instanceof TileConstructionMarker || subTile instanceof TileBuilder) {
				BlueprintBase blueprint = null;
				EnumFacing orientation = EnumFacing.EAST;

				if (subTile instanceof TileConstructionMarker) {
					TileConstructionMarker marker = (TileConstructionMarker) subTile;
					blueprint = ItemBlueprint.loadBlueprint(marker.itemBlueprint);
					orientation = marker.direction;
				} else if (subTile instanceof TileBuilder) {
					TileBuilder builder = (TileBuilder) subTile;
					blueprint = ItemBlueprint.loadBlueprint(builder.getStackInSlot(0));
					orientation =((EnumFacing)architect.getWorld().getBlockState(subBlock).getValue(BlockBuildCraft.FACING_PROP)).getOpposite();
				}

				if (blueprint != null) {
					writingBlueprint.addSubBlueprint(
							blueprint,
							subTile.getPos().add(-architect.getBox().xMin, -architect.getBox().yMin, -architect.getBox().zMin),
							orientation);
				}

				subIndex++;
			} else {
				subIndex++;
			}
		} else if (currentSubReader != null) {
			currentSubReader.iterate();

			if (currentSubReader.isDone()) {
				writingBlueprint.addSubBlueprint
						(currentSubReader.getBlueprint(),
								currentSubReader.architect.getPos().add(-architect.getBox().xMin, -architect.getBox().yMin, -architect.getBox().zMin),
								((EnumFacing)architect.getWorld().getBlockState(currentSubReader.architect.getPos()).getValue(BlockBuildCraft.FACING_PROP)).getOpposite());

				currentSubReader = null;
				subIndex++;
			}
		} else if (blockScanner != null && blockScanner.blocksLeft() != 0) {
			for (BlockPos index : blockScanner) {
				writingBlueprint.readFromWorld(writingContext, architect,
						index);
			}

			computingTime = 1 - (float) blockScanner.blocksLeft()
					/ (float) blockScanner.totalBlocks();

			if (blockScanner.blocksLeft() == 0) {
				writingBlueprint.readEntitiesFromWorld(writingContext, architect);

				Translation transform = new Translation();

				transform.x = -writingContext.surroundingBox().pMin().x;
				transform.y = -writingContext.surroundingBox().pMin().y;
				transform.z = -writingContext.surroundingBox().pMin().z;

				writingBlueprint.translateToBlueprint(transform);

				EnumFacing o = ((EnumFacing)architect.getWorld().getBlockState(architect.getPos()).getValue(BlockBuildCraft.FACING_PROP)).getOpposite();

				writingBlueprint.rotate = architect.readConfiguration.rotate;
				writingBlueprint.excavate = architect.readConfiguration.excavate;

				if (writingBlueprint.rotate) {
					if (o == EnumFacing.EAST) {
						// Do nothing
					} else if (o == EnumFacing.SOUTH) {
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
					} else if (o == EnumFacing.WEST) {
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
					} else if (o == EnumFacing.NORTH) {
						writingBlueprint.rotateLeft(writingContext);
					}
				}
			}
		} else if (blockScanner != null && writingBlueprint.getData() != null) {
			createBlueprint();

			done = true;
		}
	}

	private BlueprintBase getBlueprint() {
		return writingBlueprint;
	}

	public void createBlueprint() {
		writingBlueprint.id.name = architect.name;
		writingBlueprint.author = architect.currentAuthorName;
		BuildCraftBuilders.serverDB.add(writingBlueprint);

		if (parentBlueprint == null) {
			// TODO: This is hacky, should probably be done in the architect
			// itself.
			architect.setInventorySlotContents(1, writingBlueprint.getStack());
			architect.setInventorySlotContents(0, null);
		}
	}

	public boolean isDone() {
		return done;
	}

	public float getComputingProgressScaled() {
		float sections = architect.subBlueprints.size() + 1;

		float processed = subIndex;

		if (currentSubReader != null) {
			processed += currentSubReader.getComputingProgressScaled();
		}

		processed += computingTime;

		return processed / sections;
	}
}
