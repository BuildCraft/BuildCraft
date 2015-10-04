/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockLaserTable extends BlockBuildCraft implements ILaserTargetBlock {
	public static final int TABLE_MAX = 6;

	public BlockLaserTable() {
		super(Material.iron, BuildCraftProperties.LASER_TABLE_TYPE);

		setBlockBounds(0, 0, 0, 1, 8F / 16F, 1);
		setHardness(10F);
		setCreativeTab(BCCreativeTab.get("main"));
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float par7, float par8,
									float par9) {
		if (super.onBlockActivated(world, pos, state, entityplayer, side, par7, par8, par9)) {
			return true;
		}

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		if (!world.isRemote) {
			int ord = BuildCraftProperties.LASER_TABLE_TYPE.getValue(state).ordinal();
			entityplayer.openGui(BuildCraftSilicon.instance, ord, world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		EnumLaserTableType tableType = BuildCraftProperties.LASER_TABLE_TYPE.getValue(state);
		switch (tableType) {
			case ASSEMBLY_TABLE:
				return new TileAssemblyTable();
			case ADVANCED_CRAFTING_TABLE:
				return new TileAdvancedCraftingTable();
			case INTEGRATION_TABLE:
				return new TileIntegrationTable();
			case CHARGING_TABLE:
				return new TileChargingTable();
			case PROGRAMMING_TABLE:
				return new TileProgrammingTable();
			case STAMPING_TABLE:
				return new TileStampingTable();
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return BuildCraftProperties.LASER_TABLE_TYPE.getValue(state).ordinal();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List) {
		for (int i = 0; i < TABLE_MAX; i++) {
			par3List.add(new ItemStack(this, 1, i));
		}
	}

	// @Override
	// @SideOnly(Side.CLIENT)
	// public String[] getIconBlockNames() {
	// return new String[] { "BuildCraft|Silicon:assemblyTable", "BuildCraft|Silicon:advancedCraftingTable",
	// "BuildCraft|Silicon:integrationTable",
	// "BuildCraft|Silicon:chargingTable", "BuildCraft|Silicon:programmingTable", "BuildCraft|Silicon:stampingTable" };
	// }
	// TODO (PASS 0): Give the laser table a block model
}
