/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftSilicon;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockLaserTable extends BlockBuildCraft implements ILaserTargetBlock {
	public static final PropertyEnum TYPE = BuildCraftProperties.LASER_TABLE_TYPE;

	public BlockLaserTable() {
		super(Material.iron, new PropertyEnum[]{TYPE});
		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(10F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftSilicon.instance, EnumLaserTableType.getType(state).ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		switch (EnumLaserTableType.getType(state)) {
			case ASSEMBLY_TABLE:
				return new TileAssemblyTable();
			case ADVANCED_CRAFTING_TABLE:
				return new TileAdvancedCraftingTable();
			case INTEGRATION_TABLE:
				return new TileIntegrationTable();
            case CHARGING_TABLE:
                return new TileChargingTable();
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return this.getMetaFromState(state);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List) {
        for (EnumLaserTableType type : EnumLaserTableType.values()) {
            par3List.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, type))));
        }
	}

	@Override
	public boolean isFullCube() { return false; }
}
