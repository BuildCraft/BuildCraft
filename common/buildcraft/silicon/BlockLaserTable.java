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
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.Utils;

public class BlockLaserTable extends BlockBuildCraft implements ILaserTargetBlock {
    protected static final int TABLE_MAX = 5;

	private static final String[] ICON_NAMES = {"assemblytable", "advworkbenchtable", "integrationtable", "chargingtable", "programmingtable"};
	@SideOnly(Side.CLIENT)
	private IIcon[][] icons;

	public BlockLaserTable() {
		super(Material.iron);

		setBlockBounds(0, 0, 0, 1, 9F / 16F, 1);
		setHardness(10F);
		setCreativeTab(CreativeTabBuildCraft.BLOCKS.get());
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		// Drop through if the player is sneaking
		if (entityplayer.isSneaking()) {
			return false;
		}

		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, this, world.getBlockMetadata(i, j, k));
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		if (!world.isRemote) {
			int meta = world.getBlockMetadata(i, j, k);
			entityplayer.openGui(BuildCraftSilicon.instance, meta, world, i, j, k);
		}
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public IIcon getIcon(int side, int meta) {
        if (meta >= TABLE_MAX) {
            return null;
        }

		int s = side > 1 ? 2 : side;
		return icons[meta][s];
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		switch (metadata) {
			case 0:
				return new TileAssemblyTable();
			case 1:
				return new TileAdvancedCraftingTable();
			case 2:
				return new TileIntegrationTable();
            case 3:
                return new TileChargingTable();
			case 4:
				return new TileProgrammingTable();
		}
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}

	@Override
	public int damageDropped(int par1) {
		return par1;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List par3List) {
        for (int i = 0; i < TABLE_MAX; i++) {
            par3List.add(new ItemStack(this, 1, i));
        }
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		icons = new IIcon[TABLE_MAX][];
        for (int i = 0; i < TABLE_MAX; i++) {
            icons[i] = new IIcon[3];
			icons[i][0] = par1IconRegister.registerIcon("buildcraft:" + ICON_NAMES[i] + "_bottom");
			icons[i][1] = par1IconRegister.registerIcon("buildcraft:" + ICON_NAMES[i] + "_top");
			icons[i][2] = par1IconRegister.registerIcon("buildcraft:" + ICON_NAMES[i] + "_side");
        }
	}
}
