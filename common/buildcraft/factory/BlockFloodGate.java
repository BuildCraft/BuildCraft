/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockFloodGate extends BlockBuildCraft {
	private IIcon valve, transparent;

	public BlockFloodGate() {
		super(Material.iron);
		setPassCount(2);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileFloodGate();
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int side, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, side, par7, par8, par9)) {
			return true;
		}

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		TileEntity tile = world.getTileEntity(i, j, k);

		if (tile instanceof TileFloodGate) {
			TileFloodGate floodGate = (TileFloodGate) tile;
			// Restart the flood gate if it's a wrench
			Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {
				if (side == 1) {
					floodGate.rebuildQueue();
				} else {
					floodGate.switchSide(ForgeDirection.getOrientation(side));
				}

				((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileFloodGate) {
			((TileFloodGate) tile).onNeighborBlockChange(block);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		super.registerBlockIcons(register);
		valve = register.registerIcon("buildcraftfactory:floodGateBlock/valve");
		transparent = register.registerIcon("buildcraftcore:misc/transparent");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		if (renderPass == 1) {
			if (side != 1) {
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof TileFloodGate) {
					return ((TileFloodGate) tile).isSideBlocked(side) ? transparent : valve;
				}
			}
			return transparent;
		} else {
			return super.getIcon(world, x, y, z, side);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if (renderPass == 1) {
			if (side == 1) {
				return null;
			}
			return valve;
		} else {
			return super.getIcon(side, metadata);
		}
	}
}
