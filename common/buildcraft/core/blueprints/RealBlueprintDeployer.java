/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.blueprints;

import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.api.blueprints.Translation;
import buildcraft.builders.blueprints.BlueprintDatabase;
import buildcraft.builders.blueprints.BlueprintId;
import buildcraft.builders.blueprints.BlueprintId.Kind;

public class RealBlueprintDeployer extends BlueprintDeployer {

	@Override
	public void deployBlueprint(World world, int x, int y, int z,
			ForgeDirection dir, File file) {

		Blueprint bpt = (Blueprint) BlueprintDatabase.load(file);
		bpt.id = new BlueprintId();
		bpt.id.kind = Kind.Blueprint;

		BptContext context = bpt.getContext(world, bpt.getBoxForPos(x, y, z));

		if (bpt.rotate) {
			if (dir == ForgeDirection.EAST) {
				// Do nothing
			} else if (dir == ForgeDirection.SOUTH) {
				bpt.rotateLeft(context);
			} else if (dir == ForgeDirection.WEST) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			} else if (dir == ForgeDirection.NORTH) {
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
				bpt.rotateLeft(context);
			}
		}

		Translation transform = new Translation();

		transform.x = x - bpt.anchorX;
		transform.y = y - bpt.anchorY;
		transform.z = z - bpt.anchorZ;

		bpt.transformToWorld(transform);

		BptBuilderBlueprint deployer = new BptBuilderBlueprint(bpt, world, x, y, z);
		deployer.initialize();
		deployer.deploy();
	}

	@Override
	public void deployBlueprintFromFileStream(World world, int x, int y, int z,
			ForgeDirection dir, byte [] data) {

		NBTTagCompound nbt;
		try {
			nbt = CompressedStreamTools.decompress(data);
			BlueprintBase blueprint = BlueprintBase.loadBluePrint(nbt);
			Blueprint bpt = (Blueprint) blueprint;
			bpt.setData(data);
			bpt.id = new BlueprintId();
			bpt.id.kind = Kind.Blueprint;

			BptContext context = bpt.getContext(world, bpt.getBoxForPos(x, y, z));

			if (bpt.rotate) {
				if (dir == ForgeDirection.EAST) {
					// Do nothing
				} else if (dir == ForgeDirection.SOUTH) {
					bpt.rotateLeft(context);
				} else if (dir == ForgeDirection.WEST) {
					bpt.rotateLeft(context);
					bpt.rotateLeft(context);
				} else if (dir == ForgeDirection.NORTH) {
					bpt.rotateLeft(context);
					bpt.rotateLeft(context);
					bpt.rotateLeft(context);
				}
			}
			Translation transform = new Translation();

			transform.x = x - bpt.anchorX;
			transform.y = y - bpt.anchorY;
			transform.z = z - bpt.anchorZ;

			bpt.transformToWorld(transform);

			BptBuilderBlueprint deployer = new BptBuilderBlueprint(bpt, world, x, y, z);
			deployer.initialize();
			deployer.deploy();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}

