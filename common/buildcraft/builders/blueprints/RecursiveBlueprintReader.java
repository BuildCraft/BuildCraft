/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.blueprints;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.Translation;
import buildcraft.api.core.BlockIndex;
import buildcraft.builders.ItemBlueprint;
import buildcraft.builders.ItemBlueprintStandard;
import buildcraft.builders.ItemBlueprintTemplate;
import buildcraft.builders.TileArchitect;
import buildcraft.builders.TileBuilder;
import buildcraft.builders.TileConstructionMarker;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.utils.BlockScanner;

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

	private BlueprintBase parentBlueprint;

	public RecursiveBlueprintReader(TileArchitect iArchitect) {
		architect = iArchitect;
		ItemStack stack = architect.getStackInSlot(0);

		if (stack != null && stack.getItem() instanceof ItemBlueprint && architect.box.isInitialized()) {
			blockScanner = new BlockScanner(architect.box, architect.getWorldObj(), SCANNER_ITERATION);

			if (stack.getItem() instanceof ItemBlueprintStandard) {
				writingBlueprint = new Blueprint(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			} else if (stack.getItem() instanceof ItemBlueprintTemplate) {
				writingBlueprint = new Template(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			}

			writingContext = writingBlueprint.getContext(architect.getWorldObj(), architect.box);
			writingContext.readConfiguration = architect.readConfiguration;

			writingBlueprint.id.name = architect.name;
			writingBlueprint.author = architect.currentAuthorName;
			writingBlueprint.anchorX = architect.xCoord - architect.box.xMin;
			writingBlueprint.anchorY = architect.yCoord - architect.box.yMin;
			writingBlueprint.anchorZ = architect.zCoord - architect.box.zMin;
		} else {
			done = true;
		}
	}

	protected RecursiveBlueprintReader(TileArchitect iArchitect, BlueprintBase iParentBlueprint) {
		parentBlueprint = iParentBlueprint;
		architect = iArchitect;

		if (architect.box.isInitialized()) {
			blockScanner = new BlockScanner(architect.box, architect.getWorldObj(), SCANNER_ITERATION);

			if (parentBlueprint instanceof Blueprint) {
				writingBlueprint = new Blueprint(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			} else if (parentBlueprint instanceof Template) {
				writingBlueprint = new Template(architect.box.sizeX(), architect.box.sizeY(), architect.box.sizeZ());
			}

			writingContext = writingBlueprint.getContext(architect.getWorldObj(), architect.box);
			writingContext.readConfiguration = architect.readConfiguration;

			writingBlueprint.id.name = architect.name;
			writingBlueprint.author = architect.currentAuthorName;
			writingBlueprint.anchorX = architect.xCoord - architect.box.xMin;
			writingBlueprint.anchorY = architect.yCoord - architect.box.yMin;
			writingBlueprint.anchorZ = architect.zCoord - architect.box.zMin;
		}
	}

	public void iterate() {
		if (done) {
			return;
		} else if (currentSubReader == null && subIndex < architect.subBlueprints.size()) {
			BlockIndex subBlock = architect.subBlueprints.get(subIndex);

			TileEntity subTile = architect.getWorldObj().getTileEntity(subBlock.x, subBlock.y,
					subBlock.z);

			if (subTile instanceof TileArchitect) {
				TileArchitect subArchitect = (TileArchitect) subTile;
				currentSubReader = new RecursiveBlueprintReader(subArchitect, writingBlueprint);
			} else if (subTile instanceof TileConstructionMarker || subTile instanceof TileBuilder) {
				BlueprintBase blueprint = null;
				ForgeDirection orientation = ForgeDirection.EAST;

				if (subTile instanceof TileConstructionMarker) {
					TileConstructionMarker marker = (TileConstructionMarker) subTile;
					blueprint = ItemBlueprint.loadBlueprint(marker.itemBlueprint);
					orientation = marker.direction;
				} else if (subTile instanceof TileBuilder) {
					TileBuilder builder = (TileBuilder) subTile;
					blueprint = ItemBlueprint.loadBlueprint(builder.getStackInSlot(0));
					orientation = ForgeDirection.values()[architect.getWorldObj().getBlockMetadata(subBlock.x, subBlock.y,
							subBlock.z)].getOpposite();
				}

				if (blueprint != null) {
					writingBlueprint.addSubBlueprint(
							blueprint,
							subTile.xCoord - architect.getBox().xMin,
							subTile.yCoord - architect.getBox().yMin,
							subTile.zCoord - architect.getBox().zMin,
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
								currentSubReader.architect.xCoord - architect.getBox().xMin,
								currentSubReader.architect.yCoord - architect.getBox().yMin,
								currentSubReader.architect.zCoord - architect.getBox().zMin,
								ForgeDirection.values()[
										currentSubReader.architect.getWorldObj().getBlockMetadata(
												currentSubReader.architect.xCoord,
												currentSubReader.architect.yCoord,
												currentSubReader.architect.zCoord)].getOpposite());

				currentSubReader = null;
				subIndex++;
			}
		} else if (blockScanner != null && blockScanner.blocksLeft() != 0) {
			for (BlockIndex index : blockScanner) {
				writingBlueprint.readFromWorld(writingContext, architect,
						index.x, index.y, index.z);
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

				ForgeDirection o = ForgeDirection.values()[architect.getWorldObj().getBlockMetadata(
						architect.xCoord, architect.yCoord, architect.zCoord)].getOpposite();

				writingBlueprint.rotate = architect.readConfiguration.rotate;
				writingBlueprint.excavate = architect.readConfiguration.excavate;

				if (writingBlueprint.rotate) {
					if (o == ForgeDirection.EAST) {
						// Do nothing
					} else if (o == ForgeDirection.SOUTH) {
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
					} else if (o == ForgeDirection.WEST) {
						writingBlueprint.rotateLeft(writingContext);
						writingBlueprint.rotateLeft(writingContext);
					} else if (o == ForgeDirection.NORTH) {
						writingBlueprint.rotateLeft(writingContext);
					}
				}
			}
		} else if (blockScanner != null) {
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
		NBTTagCompound nbt = writingBlueprint.getNBT();
		BuildCraftBuilders.serverDB.add(writingBlueprint.id, nbt);

		if (parentBlueprint == null) {
			architect.storeBlueprintStack(writingBlueprint.getStack());
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
