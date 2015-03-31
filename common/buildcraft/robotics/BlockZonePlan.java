/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import buildcraft.BuildCraftRobotics;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.robotics.map.MapWorld;

public class BlockZonePlan extends BlockBuildCraft {
	public BlockZonePlan() {
		super(Material.iron);
		setRotatable(true);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileZonePlan();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7,
			float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftRobotics.instance, GuiIds.MAP,
					world, i, j, k);
		}

		return true;
	}
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);

		if (!world.isRemote) {
			int r = TileZonePlan.RESOLUTION >> 4;

			int cox = x >> 4;
			int coz = z >> 4;
			MapWorld w = BuildCraftRobotics.manager.getWorld(world);

			for (int cx = -r; cx < r; cx++) {
				for (int cz = -r; cz < r; cz++) {
					int dist = cx * cx + cz * cz;
					w.queueChunkForUpdateIfEmpty(cox + cx, coz + cz, dist);
				}
			}
		}
	}
}
